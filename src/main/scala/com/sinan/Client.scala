package com.sinan

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Client {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: FiniteDuration = 300.millis

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val bearerJsonReader: JsonReader[BearerResponse] = new JsonReader[BearerResponse] {
    def read(value: JsValue): BearerResponse = {
      value.asJsObject().getFields("token_type", "access_token") match {
        case Seq(JsString(token_type), JsString(access_token)) =>
          BearerResponse(token_type, access_token)
      }
    }
  }

  def parseJson(str: String): BearerResponse = {
    str.parseJson.convertTo[BearerResponse]
  }

  def main(args: Array[String]): Unit = {
    val responseFuture: Future[HttpResponse] = obtainBearerToken(
      Properties.readString("consumer.key"),
      Properties.readString("consumer.secret")
    )

    responseFuture
      .onComplete {
        case Success(res) =>
          val bearerFuture: Future[BearerResponse] = Unmarshal(res.entity).to[String].map{ jsonString =>
            parseJson(jsonString)
          }
          bearerFuture.onComplete{
            case Success(bearer) =>
              val bearerHeader = Authorization(OAuth2BearerToken(bearer.access_token))
              searchRequest(bearerHeader, "bojack").onComplete{
                // TODO: AKKA-HTTP intended for streaming, this is not streaming!
                case Success(tweets) => tweets.toStrict(timeout).map{ t => println(t.entity) }
              }
          }
        case Failure(_) => sys.error("Failed to obtain Bearer Token")
      }
  }

  // bearer token for OAUTH2
  // try implementing user-based auth:
  // https://developer.twitter.com/en/docs/basics/authentication/overview
  def obtainBearerToken(consumerKey: String, consumerSecret: String): Future[HttpResponse] = {
    val basicHeader = Authorization(BasicHttpCredentials(consumerKey, consumerSecret))

    Http().singleRequest(HttpRequest(
      uri = Uri("https://api.twitter.com/oauth2/token"),
      method = HttpMethods.POST,
      headers = List(basicHeader),
      entity = akka.http.scaladsl.model.FormData(Map("grant_type" -> "client_credentials")).toEntity(HttpCharsets.`UTF-8`),
      protocol = HttpProtocols.`HTTP/1.1`)
    )
  }

  // TODO: https://stream.twitter.com/1.1/statuses/filter.json
  // ONLY AVAILABLE VIA OAUTH1.1
  // https://developer.twitter.com/en/docs/tweets/filter-realtime/api-reference/post-statuses-filter.html

  def searchRequest(auth: Authorization, searchTerm: String): Future[HttpResponse] = {
    Http().singleRequest(HttpRequest(
      uri = Uri(s"https://api.twitter.com/1.1/search/tweets.json?q=$searchTerm"),
      method = HttpMethods.GET,
      headers = List(auth))
    )
  }
}

case class BearerResponse(token_type: String, access_token: String)

