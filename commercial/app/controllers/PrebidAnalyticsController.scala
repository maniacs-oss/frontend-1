package commercial.controllers

import java.nio.ByteBuffer

import awswrappers.kinesisfirehose._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.kinesisfirehose.model.{PutRecordRequest, Record}
import com.amazonaws.services.kinesisfirehose.{AmazonKinesisFirehoseAsync, AmazonKinesisFirehoseAsyncClientBuilder}
import common.Logging
import conf.Configuration.aws.region
import conf.switches.Switches.prebidAnalytics
import model.Cached.WithoutRevalidationResult
import model.{CacheTime, Cached, TinyResponse}
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toBytes
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class PrebidAnalyticsController(val controllerComponents: ControllerComponents) extends BaseController with Logging {

  private implicit val ec: ExecutionContext = controllerComponents.executionContext

  // todo credentials
  // todo cloudform firehose
  private val firehose: AmazonKinesisFirehoseAsync = {
    val credentialsProvider = new ProfileCredentialsProvider("developerPlayground")
    AmazonKinesisFirehoseAsyncClientBuilder
      .standard()
      .withCredentials(credentialsProvider)
      .withRegion(region)
      .build()
  }

  // todo
  private val deliveryStreamName = "KelvinTestFirehose"

  private def putRequest(data: Array[Byte]): PutRecordRequest = {
    val record = new Record().withData(ByteBuffer.wrap(data))
    new PutRecordRequest().withDeliveryStreamName(deliveryStreamName).withRecord(record)
  }

  private def serve404[A](implicit request: Request[A]) =
    Cached(CacheTime.NotFound)(WithoutRevalidationResult(NotFound))

  def insert(): Action[JsValue] = Action(parse.json) { implicit request =>
    if (prebidAnalytics.isSwitchedOn) {
      val result = firehose.putRecordFuture(putRequest(toBytes(request.body)))
      result.failed foreach {
        case NonFatal(e) => log.error(s"Failed to put '${request.body}'", e)
      }
      TinyResponse.noContent()
    } else
      serve404
  }

  def getOptions: Action[AnyContent] = Action { implicit request =>
    if (prebidAnalytics.isSwitchedOn)
      TinyResponse.noContent(Some("OPTIONS, PUT"))
    else
      serve404
  }
}
