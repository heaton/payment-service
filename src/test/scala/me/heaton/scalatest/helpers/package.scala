package me.heaton.scalatest

import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

package object helpers {

  trait Specification extends AnyWordSpec with should.Matchers with OneInstancePerTest

}
