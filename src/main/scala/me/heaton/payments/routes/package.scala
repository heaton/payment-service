package me.heaton.payments

package object routes {

  case class ValidationError(message: String) extends RuntimeException

  def validate(condition: Boolean, message: String): Unit =
    if (condition) {} else throw ValidationError(message)
}
