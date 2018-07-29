package org.hatdex.serverless.aws

import java.time.ZonedDateTime

import play.api.libs.json._

import scala.util.{Success, Try}


case class SNSEvent[T](
  Type: String,
  MessageId: String,
  TopicArn: String,
  Subject: Option[String],
  Message: T,
  Timestamp: ZonedDateTime,
  SignatureVersion: String,
  Signature: String,
  SigningCertUrl: String,
  UnsubscribeUrl: String,
  MessageAttributes: JsValue)

case class EventRecord[T](
  EventSource: String,
  EventVersion: String,
  EventSubscriptionArn: String,
  Sns: Option[SNSEvent[T]])

case class Event[T](Records: Seq[EventRecord[T]])

trait EventJsonProtocol {
  private def generateSnsEventReads[T](implicit reads: Reads[T]): Reads[SNSEvent[T]] = {
    Json.reads[SNSEvent[T]]
  }

  implicit def snsEventReads[T](implicit reads: Reads[T]): Reads[SNSEvent[T]] = {
    val stringReads: Reads[T] = Reads.StringReads
      .map(s => Try(Json.parse(s)))
      .collect(JsonValidationError("")) {
        case Success(js) => js
      }
      .andThen(reads)

    generateSnsEventReads(stringReads)
  }
  implicit def eventRecordReads[T](implicit reads: Reads[T]): Reads[EventRecord[T]] = Json.reads[EventRecord[T]]
  implicit def eventReads[T](implicit reads: Reads[T]): Reads[Event[T]] = Json.reads[Event[T]]
}

object EventJsonProtocol extends EventJsonProtocol
