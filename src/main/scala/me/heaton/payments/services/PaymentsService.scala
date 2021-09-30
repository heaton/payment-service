package me.heaton.payments.services

import cats.effect.IO
import me.heaton.payments.models.{Payment, Payments}
import me.heaton.payments.repositories.{AccountRepository, PaymentEventRepository}
import me.heaton.payments.routes.PaymentRequest

import java.util.UUID

class PaymentsService(accountRepository: AccountRepository, paymentEventRepository: PaymentEventRepository) {

  def listPayments: IO[Payments] = for {
    account <- accountRepository.findAccount
    payments <- paymentEventRepository.findAll
    balance = account.balance - payments.map(_.amount).sum
  } yield Payments(balance, payments)

  def save(paymentRequest: PaymentRequest):IO[Payment] =
    paymentEventRepository.save(Payment(UUID.randomUUID(), paymentRequest.date, paymentRequest.amount, "Pending", None))
}
