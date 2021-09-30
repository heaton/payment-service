package me.heaton.payments.routes

import java.time.LocalDate

case class PaymentRequest(amount: BigDecimal, date: LocalDate)
