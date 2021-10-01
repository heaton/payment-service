package me.heaton

import cats.Functor
import cats.data.EitherT

import java.time.LocalDate

package object payments {

  implicit class DateWrap(date: LocalDate) {
    def +(days: Int): LocalDate = date.plusDays(days)
  }

  def liftE[F[_], B](fb: F[B])(implicit F: Functor[F]): EitherT[F, Throwable, B] = EitherT.liftF[F, Throwable, B](fb)
}
