package me.heaton.payments

import cats.Monad
import cats.implicits._
import org.http4s.server._
import org.http4s.{EntityEncoder, InvalidMessageBodyFailure, Response, Status}

object ErrorHandler {

  private val logger = org.log4s.getLogger(getClass)

  def apply[F[_]](implicit F: Monad[F]): ServiceErrorHandler[F] = req => {
//    case InvalidMessageBodyFailure(details, Some(cause)) => {
//      logger.info(cause)(details)
//      Response[F](Status.UnprocessableEntity, req.httpVersion)
//        .withEntity(s"$details, ${cause.getMessage}")(EntityEncoder.stringEncoder[F])
//        .pure[F]
//    }
    case e => DefaultServiceErrorHandler(F)(req)(e)
  }
}
