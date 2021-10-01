package me.heaton.payments.routes

import cats.data.EitherT
import cats.effect.IO
import io.circe.generic.auto._
import me.heaton.payments.excpetions._
import me.heaton.payments.models.ActionResult
import me.heaton.payments.services.PaymentsService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, Response}

import java.util.UUID

class PaymentsRoute(paymentsService: PaymentsService) {

  def routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "payments" => paymentsService.listPayments.flatMap(Ok(_))
    case req@POST -> Root / "payments" => for {
      request <- req.as[PaymentRequest]
      payment <- paymentsService.save(request)
      resp <- Created(payment)
    } yield resp
    case PUT -> Root / "payments" / paymentId => process(paymentId)(paymentsService.process)
    case req@PATCH -> Root / "payments" / paymentId => for {
      reason <- req.as[CancelRequest].attempt.map(_.toOption.map(_.reason))
      resp <- process(paymentId)(paymentsService.update(reason))
    } yield resp
  }

  private def process(paymentId: String)(processor: UUID => EitherT[IO, PaymentError, ActionResult]) = (for {
    id <- EitherT(IO(UUID.fromString(paymentId)).attempt)
    actionResult <- processor(id)
    resp <- EitherT.liftF[IO, Throwable, Response[IO]](Ok(actionResult))
  } yield resp).valueOrF {
    case PaymentClosedException => BadRequest(s"payment $paymentId is Closed")
    case _ => NotFound(s"payment $paymentId doesn't exist")
  }
}
