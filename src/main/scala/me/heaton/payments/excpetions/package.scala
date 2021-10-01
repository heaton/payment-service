package me.heaton.payments

package object excpetions {

  sealed trait PaymentError extends RuntimeException

  object PaymentNotFoundException extends PaymentError

  object PaymentClosedException extends PaymentError

  object InsufficientBalance extends PaymentError

}
