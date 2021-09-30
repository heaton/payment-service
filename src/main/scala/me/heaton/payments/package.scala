package me.heaton

import java.time.LocalDate

package object payments {

  implicit class DateWrap(date: LocalDate) {
    def +(days: Int): LocalDate = date.plusDays(days)
  }

}
