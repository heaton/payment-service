package me.heaton.payments.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import io.circe.generic.auto._
import me.heaton.payments.services.PaymentsService

class PaymentsRoute(paymentsService: PaymentsService) {

  def routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "payments" => paymentsService.listPayments.flatMap(Ok(_))
    case req@POST -> Root / "payments" => for {
      request <- req.as[PaymentRequest]
      payment <- paymentsService.save(request)
      resp <- Created(payment)
    } yield resp
  }

}
