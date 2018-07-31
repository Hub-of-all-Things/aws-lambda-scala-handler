package org.hatdex.serverless.aws

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.Context
import org.hatdex.serverless.aws.proxy.ErrorResponse
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

abstract class LambdaHandler[I, O]()(implicit val inputReads: Reads[I], val outputWrites: Writes[O]) extends LambdaStreamHandler[I, O] {

  final def handle(input: InputStream, output: OutputStream, context: Context): Unit =
    handle(handleAsync _, handleError _)(input, output, context)

  protected val executionContext: ExecutionContext = ExecutionContext.global
  final protected def handleAsync(i: I, c: Context): Future[O] = Future.fromTry(handle(i, c))

  def handle(i: I,  c: Context): Try[O]

  // There is no way by default to recover from error to a valid response type
  protected def handleError(errorResponse: ErrorResponse): O = throw errorResponse
}

abstract class LambdaHandlerAsync[I, O]()(implicit val inputReads: Reads[I], val outputWrites: Writes[O], val executionContext: ExecutionContext) extends LambdaStreamHandler[I, O] {
  final def handle(input: InputStream, output: OutputStream, context: Context): Unit =
    handle(handleAsync _, handleError _)(input, output, context)

  final protected def handleAsync(i: I, c: Context): Future[O] = handle(i, c)

  def handle(i: I, c: Context): Future[O]

  // There is no way by default to recover from error to a valid response type
  protected def handleError(errorResponse: ErrorResponse): O = throw errorResponse
}
