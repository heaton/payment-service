package me.heaton.payments.repositories

import cats.effect.IO
import me.heaton.payments.models.Payment

import scala.collection.mutable.ListBuffer

class PaymentEventRepository {
  private val store = new ListBuffer[Payment]

  def save(payment: Payment): IO[Payment] = IO {
    store += payment
    payment
  }

  def findAll: IO[List[Payment]] = IO(store.toList)

  def clear: Unit =
    store.clear

}
