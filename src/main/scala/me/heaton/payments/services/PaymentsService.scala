package me.heaton.payments.services

import cats.data.EitherT
import cats.data.EitherT.liftF
import cats.effect.IO
import me.heaton.payments.excpetions._
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

  def update(reason: Option[String])(paymentId: UUID): EitherT[IO, PaymentError, ActionResult] = for {
    payment <- EitherT(paymentEventRepository.findById(paymentId).map(validate))
    closedPayment <- liftF(paymentEventRepository.save(payment.copy(status = "Closed", reason = reason, createdTime = Instant.now)))
    payments <- liftF(listPayments)
  } yield ActionResult(payments.balance, closedPayment.id, closedPayment.status, closedPayment.reason)

  def process: UUID => EitherT[IO, PaymentError, ActionResult] = update(None)

  private def validate(paymentEvents: List[Payment]) = for {
    _ <- Either.cond(paymentEvents.nonEmpty, Unit, PaymentNotFoundException)
    _ <- Either.cond(!paymentEvents.exists(_.status == "Closed"), Unit, PaymentClosedException)
  } yield paymentEvents.head

}
