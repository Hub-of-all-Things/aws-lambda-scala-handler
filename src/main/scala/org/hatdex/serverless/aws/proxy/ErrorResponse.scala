package org.hatdex.serverless.aws.proxy

sealed abstract class ErrorResponse(val status: Int, message: String, cause: Throwable = None.orNull) extends RuntimeException(message, cause) {
  def getStatus: Int = status
}

case class BadRequestError(message: String, cause: Throwable = None.orNull) extends ErrorResponse(400, message, cause)

case class InternalServerError(message: String, cause: Throwable = None.orNull) extends ErrorResponse(500, message, cause)

