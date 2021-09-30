package me.heaton.payments.helpers

import me.heaton.payments.models.Payment
import me.heaton.payments.repositories.PaymentEventRepository
import cats.implicits.toTraverseOps

trait RepositoryHelper {

  protected val paymentEventRepository: PaymentEventRepository

  protected object given {
    def data(payments: Payment*): Unit = {
      payments.toList.map(paymentEventRepository.save).sequence.unsafeRunSync()
    }
  }
}
