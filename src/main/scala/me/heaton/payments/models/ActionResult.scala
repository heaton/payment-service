package me.heaton.payments.models

import java.util.UUID

case class ActionResult(balance: BigDecimal, paymentId: UUID, status: String, reason: Option[String])
