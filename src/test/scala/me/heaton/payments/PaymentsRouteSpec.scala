package me.heaton.payments

import io.circe.generic.auto._
import me.heaton.payments.models.{Payment, Payments}
import me.heaton.payments.routes.PaymentRequest
import me.heaton.scalatest.helpers.RouteSpecification
import org.http4s.circe.CirceEntityDecoder._
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDate

class PaymentsRouteSpec extends RouteSpecification with BeforeAndAfterEach with PaymentsServer {

  private val today = LocalDate.now

  override def afterEach() {
    paymentEventRepository.clear
  }

  "Payments Route" should {
    "get payments" should {
      "return no payment if no payment made" in {
        payments get "/payments" check {
          status shouldEqual Ok
          body[Payments] shouldEqual Payments(1000, List())
        }
      }

      "return 1 pending payment if a payment created" in {
        payments post "/payments" withJson PaymentRequest(200, today) check {
          status shouldEqual Created
          val payment = body[Payment]
          payment shouldEqual Payment(payment.id, today, 200, "Pending", None, payment.createdTime)

          payments get "/payments" check {
            status shouldEqual Ok
            body[Payments] shouldEqual Payments(800, List(payment))
          }
        }
      }
    }
  }

}
