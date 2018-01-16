package org.hatdex.serverless.aws.proxy

import scala.util.Try

case class RequestInput(body: String)

case class ProxyRequest[T](
  path: String,
  httpMethod: String,
  headers: Option[Map[String, String]] = None,
  queryStringParameters: Option[Map[String, String]] = None,
  stageVariables: Option[Map[String, String]] = None,
  body: Option[T] = None
)

case class ProxyResponse[T](
  statusCode: Int,
  headers: Option[Map[String, String]] = None,
  body: Either[Option[T], ErrorResponse] = Left(None)
)

object ProxyResponse {
  protected val defaultHeaders = Some(Map("Access-Control-Allow-Origin" -> "*"))

  def apply[T](result: Try[T]): ProxyResponse[T] = {
    result.map { b => ProxyResponse(200, defaultHeaders, Left(Some(b))) }
      .recover {
        case e: ErrorResponse => ProxyResponse[T](e.getStatus, defaultHeaders, Right(e))
        case e: RuntimeException => ProxyResponse[T](500, defaultHeaders, Right(InternalServerError("Unexpected error", e)))
        case e => ProxyResponse[T](500, defaultHeaders, Right(InternalServerError("Fatal error", e)))
      }
      .get
  }

  def apply(): ProxyResponse[String] = {
    ProxyResponse(200, defaultHeaders, Left(None))
  }
}

