package me.heaton.payments.services

import cats.data.OptionT
import cats.effect.IO
import me.heaton.payments.models.{ActionResult, Payment, Payments}
import me.heaton.payments.repositories.{AccountRepository, PaymentEventRepository}
import me.heaton.payments.routes.PaymentRequest

import java.time.Instant
import java.util.UUID

class PaymentsService(accountRepository: AccountRepository, paymentEventRepository: PaymentEventRepository) {

  def listPayments: IO[Payments] = for {
    account <- accountRepository.findAccount
    paymentEvents <- paymentEventRepository.findAll
    payments = paymentEvents.groupBy(_.id).mapValues(_.maxBy(_.createdTime)).values.toList
    balance = account.balance - payments.filter(_.status == "Pending").map(_.amount).sum
  } yield Payments(balance, payments)

  def save(paymentRequest: PaymentRequest): IO[Payment] =
    paymentEventRepository.save(Payment(UUID.randomUUID(), paymentRequest.date, paymentRequest.amount, "Pending", None))

  def update(reason: Option[String])(paymentId: UUID): OptionT[IO, ActionResult] = for {
    payment <- OptionT(paymentEventRepository.findById(paymentId))
    closedPayment <- OptionT.liftF(paymentEventRepository.save(payment.copy(status = "Closed", reason = reason, createdTime = Instant.now)))
    account <- OptionT.liftF(accountRepository.findAccount)
  } yield ActionResult(account.balance, closedPayment.id, closedPayment.status, closedPayment.reason)

  def process: UUID => OptionT[IO, ActionResult] = update(None)
}
