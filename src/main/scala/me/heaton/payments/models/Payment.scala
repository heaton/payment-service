package me.heaton.payments.models

import java.time.{Instant, LocalDate}
import java.util.UUID

case class Payment(id: UUID, dateToProcess: LocalDate, amount: BigDecimal, status: String, reason: Option[String], createdTime: Instant = Instant.now)
