package org.hatdex.serverless.aws.proxy

import play.api.libs.json._

import scala.util.{Success, Try}

trait JsonProtocol {

  val errorResponseWrites: Writes[ErrorResponse] = (error: ErrorResponse) => {
    val stackTrace = Option(error.getCause)
      .map(c => c.getStackTrace.toSeq.map(s => s.toString))

    JsObject(Seq(
      "status" -> JsNumber(error.getStatus),
      "error" -> JsString(error.getClass.getSimpleName),
      "message" -> JsString(error.getMessage),
      "cause" -> Json.toJson(stackTrace)))
  }

  protected def responseBodyJsonWrites[T](implicit writes: Writes[T]): Writes[Either[Option[T], ErrorResponse]] =
    (body: Either[Option[T], ErrorResponse]) => {
      body.fold(
        content => Json.toJson(content),
        error => Json.toJson(error)(errorResponseWrites)
      )
    }


  private def generateProxyRequestReads[T](implicit reads: Reads[T]): Reads[ProxyRequest[T]] = {
    Json.reads[ProxyRequest[T]]
  }

  implicit def RequestJsonReads[T](implicit reads: Reads[T]): Reads[ProxyRequest[T]] = {
    val stringReads: Reads[T] = Reads.StringReads
      .map(s => Try(Json.parse(s)))
      .collect(JsonValidationError("")) {
        case Success(js) => js
      }
      .andThen(reads)

    generateProxyRequestReads(stringReads)
  }


  implicit def ResponseJsonWrites[T](implicit writes: Writes[T]): Writes[ProxyResponse[T]] = (response: ProxyResponse[T]) => {
    JsObject(Map(
      "statusCode" -> JsNumber(response.statusCode),
      "headers" -> Json.toJson(response.headers),
      "body" -> JsString(Json.toJson(response.body)(responseBodyJsonWrites(writes)).toString)))
  }
}

object JsonProtocol extends JsonProtocol