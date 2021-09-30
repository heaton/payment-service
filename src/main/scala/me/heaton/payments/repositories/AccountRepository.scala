package me.heaton.payments.repositories

import cats.effect.IO
import me.heaton.payments.models.Account

class AccountRepository {
  private val account = Account(1000)

  def findAccount: IO[Account] = IO(account)
}
