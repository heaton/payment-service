package me.heaton.payments.models

case class Payments(balance: BigDecimal, data: List[Payment])
