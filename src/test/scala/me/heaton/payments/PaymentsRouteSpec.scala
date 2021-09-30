package me.heaton.payments

import io.circe.generic.auto._
import me.heaton.payments.helpers.RepositoryHelper
import me.heaton.payments.models.{ActionResult, Payment, Payments}
import me.heaton.payments.routes.PaymentRequest
import me.heaton.scalatest.helpers.RouteSpecification
import org.http4s.circe.CirceEntityDecoder._
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDate
import java.util.UUID

class PaymentsRouteSpec extends RouteSpecification with BeforeAndAfterEach with PaymentsServer with RepositoryHelper {

  private val today = LocalDate.now
  private val PENDING = "Pending"
  private val CLOSED = "Closed"

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
    }

    "create payment" should {
      "return 1 pending payment if a payment created" in {
        payments post "/payments" withJson PaymentRequest(200, today) check {
          status shouldEqual Created
          val payment = body[Payment]
          payment shouldEqual Payment(payment.id, today, 200, PENDING, None, payment.createdTime)

          payments get "/payments" check {
            status shouldEqual Ok
            body[Payments] shouldEqual Payments(800, List(payment))
          }
        }
      }
    }

    "process payment" should {
      "return 1 pending payment if a payment created" in {
        val paymentId = UUID.randomUUID()
        given data Payment(paymentId, today, 200, PENDING, None)

        payments put s"/payments/$paymentId" check {
          status shouldEqual Ok
          body[ActionResult] shouldEqual ActionResult(1000, paymentId, CLOSED, None)

          payments get "/payments" check {
            status shouldEqual Ok
            val payments = body[Payments]
            payments shouldEqual Payments(1000, List(Payment(paymentId, today, 200, CLOSED, None, payments.data.head.createdTime)))
          }
        }
      }

      "return 404 if no payment found" in {
        val paymentId = UUID.randomUUID()
        payments put s"/payments/$paymentId" check {
          status shouldEqual NotFound
          body[String] shouldEqual s"payment $paymentId doesn't exist"
        }
      }

      "return 404 if given payment id isn't in uuid format" in {
        payments put s"/payments/123" check {
          status shouldEqual NotFound
          body[String] shouldEqual s"payment 123 doesn't exist"
        }
      }
    }
  }

}
