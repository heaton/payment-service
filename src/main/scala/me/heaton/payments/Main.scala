package me.heaton.payments

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.catsSyntaxApplicativeError
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import org.http4s.finagle.Finagle
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request}

class Main extends IOApp with PaymentsServer {

  private lazy val server: Resource[IO, ListeningServer] = for {
    serverResource <- Resource.make(IO {
      Http.server
        .withLabel("payment-service")
        .withHttp2
        .withHttpStats
        .serve(":8080",
          Finagle.mkService[IO](
            Kleisli { (req: Request[IO]) =>
              app.orNotFound.run(req).recoverWith(ErrorHandler[IO].apply(req))
            }
          )
        )
    })(s => IO(s.close()))
  } yield serverResource

  def run(args: List[String]): IO[ExitCode] = {
    server.use { s =>
      IO(Await.ready(s))
    }.as(ExitCode.Success)
  }

  private def app: HttpRoutes[IO] = Router("/" -> payments)

}
