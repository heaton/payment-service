package me.heaton.scalatest.helpers

import cats.Functor
import cats.effect.IO
import fs2.Stream
import io.circe._
import io.circe.syntax._
import me.heaton.payments.ErrorHandler
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._

import scala.util.DynamicVariable

trait RouteSpecification extends Specification {

  private val dv = new DynamicVariable[Response[IO]](null)

  private val errorHandler = ErrorHandler[IO]

  private[helpers] class RequestBuilder(route: HttpRoutes[IO], request: Request[IO]) {
    def withJson[T: Encoder](t: T): RequestBuilder = new RequestBuilder(route, request.withEntity(t.asJson))

    def withBody(body: String): RequestBuilder = new RequestBuilder(route, request.withBodyStream(Stream emits body.getBytes))

    def check[T](c: => T): T = dv.withValue(route.orNotFound(request).handleErrorWith(errorHandler(request)).unsafeRunSync())(c)
  }

  implicit class RouteWrap(val route: HttpRoutes[IO]) {

    import org.http4s.Method._

    def get(uri: String): RequestBuilder = new RequestBuilder(route, Request[IO](GET, Uri.unsafeFromString(uri)))

    def post(uri: String): RequestBuilder = new RequestBuilder(route, Request[IO](POST, Uri.unsafeFromString(uri)))

    def patch(uri: String): RequestBuilder = new RequestBuilder(route, Request[IO](PATCH, Uri.unsafeFromString(uri)))

    def put(uri: String): RequestBuilder = new RequestBuilder(route, Request[IO](PUT, Uri.unsafeFromString(uri)))
  }

  private def response: Response[IO] = if (dv.value ne null) dv.value else sys.error("This value is only available inside of a 'check' construct!")

  def status: Status = response.status

  def headers: Headers = response.headers

  def body[T](implicit F: Functor[IO], decoder: EntityDecoder[IO, T]): T = response.as[T].unsafeRunSync()

  def jsonBody[T: Decoder](implicit F: Functor[IO]): T = body[T]

  def stringBody: String = response.attemptAs[String](EntityDecoder.text).rethrowT.unsafeRunSync()

  def contentType: Option[`Content-Type`] = response.contentType

  val Ok: Status = Status.Ok
  val Created: Status = Status.Created
  val BadRequest: Status = Status.BadRequest
  val NotFound: Status = Status.NotFound
  val UnprocessableEntity: Status = Status.UnprocessableEntity

}