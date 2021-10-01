package me.heaton.payments.routes

import cats.Applicative
import cats.data.EitherT
import cats.effect.IO
import io.circe.generic.auto._
import me.heaton.payments.excpetions._
import me.heaton.payments.liftE
import me.heaton.payments.models.ActionResult
import me.heaton.payments.services.PaymentsService
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}

import java.util.UUID

class PaymentsRoute(paymentsService: PaymentsService) {

  def routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "payments" => paymentsService.listPayments.flatMap(Ok(_))
    case req@POST -> Root / "payments" => (for {
      request <- liftE(req.as[PaymentRequest])
      payment <- paymentsService.create(request)
      resp <- liftE(Created(payment))
    } yield resp).getOrElseF(badRequest("not enough balance"))
    case PUT -> Root / "payments" / paymentId => process(paymentId)(paymentsService.process)
    case req@PATCH -> Root / "payments" / paymentId => for {
      reason <- req.as[CancelRequest].attempt.map(_.toOption.map(_.reason))
      resp <- process(paymentId)(paymentsService.update(reason))
    } yield resp
  }

  private def process(paymentId: String)(processor: UUID => EitherT[IO, PaymentError, ActionResult]) = (for {
    id <- EitherT(IO(UUID.fromString(paymentId)).attempt)
    actionResult <- processor(id)
    resp <- liftE(Ok(actionResult))
  } yield resp).valueOrF {
    case PaymentClosedException => badRequest(s"payment $paymentId is Closed")
    case _ => NotFound(s"payment $paymentId doesn't exist")(Applicative[IO], EntityEncoder.stringEncoder)
  }

  private def badRequest(message: String) =
    BadRequest(message)(Applicative[IO], EntityEncoder.stringEncoder)
}
