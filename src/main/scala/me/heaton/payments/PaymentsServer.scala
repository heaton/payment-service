package me.heaton.payments

import cats.effect.IO
import me.heaton.payments.repositories.{AccountRepository, PaymentEventRepository}
import me.heaton.payments.routes.PaymentsRoute
import me.heaton.payments.services.PaymentsService
import org.http4s.HttpRoutes

trait PaymentsServer {

  protected val paymentEventRepository = new PaymentEventRepository

  protected def payments: HttpRoutes[IO] = {
    val accountRepository = new AccountRepository

    new PaymentsRoute(new PaymentsService(accountRepository, paymentEventRepository)).routes
  }
}
