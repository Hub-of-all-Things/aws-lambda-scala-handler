package org.hatdex.serverless.aws

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.core.JsonParseException
import org.hatdex.serverless.aws.proxy.{BadRequestError, ErrorResponse}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

trait LambdaStreamHandler[I, O] {
  protected implicit val inputReads: Reads[I]
  protected implicit val outputWrites: Writes[O]
  protected implicit val executionContext: ExecutionContext
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  protected implicit val errorResponseWrites: Writes[ErrorResponse] = proxy.ProxyJsonProtocol.errorResponseWrites

  // This function will ultimately be used as the external handler
  final def handle(handler: (I, Context) => Future[O], error: ErrorResponse ⇒ O)(input: InputStream, output: OutputStream, context: Context): Unit = {
    val body = scala.io.Source.fromInputStream(input).mkString
    val read: Try[JsResult[I]] = Try(Json.parse(body))
      .map(_.validate[I])
      .recover {
        case _: JsonParseException =>
          logger.debug("JSON parsing exception, trying to convert string to type directly")
          JsSuccess(body.asInstanceOf[I])
      }

    val handled: Future[O] = Future.fromTry(read)
      .flatMap {
        _.fold(
          error => {
            logger.warn(s"Error when parsing request body")
            val errorMessage = error.map { case (p, e) =>
              p.toJsonString -> e.map(_.message)
            }
            logger.warn(s"Object not valid: ${Json.toJson(errorMessage).toString}")
            Future.failed(BadRequestError("Object not valid", new RuntimeException(Json.toJson(errorMessage).toString())))
          },
          input => {
            handler(input, context)
          })
      }

    val asyncResult = handled map { result =>
      logger.debug(s"Handler result: $result")
      output.write(Json.toJson(result)
        .toString
        .getBytes)
    } recover {
      case e: ErrorResponse =>
        logger.warn(s"Sending error response: $e")
        output.write(Json.toJson(error(e))
          .toString
          .getBytes)
        output.close()
    } recover {
      case e =>
        logger.error(s"Unexpected error: ${e.getMessage}", e)
        output.close()
    }
    // Only await for the result for the remaining running time, leaving some time to finish
    Await.result(asyncResult, context.getRemainingTimeInMillis.milliseconds * 0.95)
  }
}
