package me.heaton.payments

import io.circe.generic.auto._
import me.heaton.payments.helpers.RepositoryHelper
import me.heaton.payments.models.{ActionResult, Payment, Payments}
import me.heaton.payments.routes.{CancelRequest, PaymentRequest}
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
        payments post "/payments" withJson PaymentRequest(200, today + 1) check {
          status shouldEqual Created
          val payment = body[Payment]
          payment shouldEqual Payment(payment.id, today + 1, 200, PENDING, None, payment.createdTime)

          payments get "/payments" check {
            status shouldEqual Ok
            body[Payments] shouldEqual Payments(800, List(payment))
          }
        }
      }

      "fail if the amount < 0" in {
        payments post "/payments" withBody """{"amount": 0, "date": "2021-10-01"}""" check {
          status shouldEqual BadRequest
          stringBody shouldEqual "amount must be > 0"
        }
      }

      "fail when balance is not sufficient" in {
        payments post "/payments" withJson PaymentRequest(1001, today) check {
          status shouldEqual BadRequest
          stringBody shouldEqual "not enough balance"
        }
      }

      "return 422 if payload is invalid" in {
        payments post "/payments" withBody "{}" check {
          status shouldEqual UnprocessableEntity
        }
      }
    }

    "process payment" should {
      "close the pending payment" in {
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

      "return the final balance" in {
        val paymentId = UUID.randomUUID()
        given data Payment(UUID.randomUUID(), today, 200, PENDING, None)
        given data Payment(paymentId, today, 200, PENDING, None)

        payments put s"/payments/$paymentId" check {
          status shouldEqual Ok
          body[ActionResult] shouldEqual ActionResult(800, paymentId, CLOSED, None)
        }
      }

      "fail if the payment has been closed" in {
        val paymentId = UUID.randomUUID()
        given data Payment(paymentId, today, 200, PENDING, None)
        given data Payment(paymentId, today, 200, CLOSED, None)

        payments put s"/payments/$paymentId" check {
          status shouldEqual BadRequest
          stringBody shouldEqual s"payment $paymentId is Closed"
        }
      }

      "return 404 if no payment found" in {
        val paymentId = UUID.randomUUID()
        payments put s"/payments/$paymentId" check {
          status shouldEqual NotFound
          stringBody shouldEqual s"payment $paymentId doesn't exist"
        }
      }

      "return 404 if given payment id isn't in uuid format" in {
        payments put s"/payments/123" check {
          status shouldEqual NotFound
          stringBody shouldEqual s"payment 123 doesn't exist"
        }
      }
    }

    "cancel payment" should {
      "cancel a pending payment without reason" in {
        val paymentId = UUID.randomUUID()
        given data Payment(paymentId, today, 200, PENDING, None)

        payments patch s"/payments/$paymentId" check {
          status shouldEqual Ok
          body[ActionResult] shouldEqual ActionResult(1000, paymentId, CLOSED, None)

          payments get "/payments" check {
            status shouldEqual Ok
            val payments = body[Payments]
            payments shouldEqual Payments(1000, List(Payment(paymentId, today, 200, CLOSED, None, payments.data.head.createdTime)))
          }
        }
      }

      "cancel a pending payment with reason" in {
        val paymentId = UUID.randomUUID()
        given data Payment(paymentId, today, 200, PENDING, None)

        payments patch s"/payments/$paymentId" withJson CancelRequest("a reason") check {
          status shouldEqual Ok
          body[ActionResult] shouldEqual ActionResult(1000, paymentId, CLOSED, Some("a reason"))

          payments get "/payments" check {
            status shouldEqual Ok
            val payments = body[Payments]
            payments shouldEqual Payments(1000, List(Payment(paymentId, today, 200, CLOSED, Some("a reason"), payments.data.head.createdTime)))
          }
        }
      }
    }
  }

}
