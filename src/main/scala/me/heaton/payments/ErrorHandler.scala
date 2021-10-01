package me.heaton.payments

import cats.Monad
import cats.implicits._
import me.heaton.payments.routes.ValidationError
import org.http4s.server._
import org.http4s.{EntityEncoder, Response, Status}

object ErrorHandler {

  private val logger = org.log4s.getLogger(getClass)

  def apply[F[_]](implicit F: Monad[F]): ServiceErrorHandler[F] = req => {
    case ValidationError(message) =>
      Response[F](Status.BadRequest, req.httpVersion)
        .withEntity(message)(EntityEncoder.stringEncoder[F])
        .pure[F]
    case e => DefaultServiceErrorHandler(F)(req)(e)
  }
}
