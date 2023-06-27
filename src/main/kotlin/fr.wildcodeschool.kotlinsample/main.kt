
package fr.wildcodeschool.kotlinsample

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import kotlinx.coroutines.*

import kotlinx.serialization.SerialName

import kotlinx.serialization.Serializable
import java.io.File

class SpaceXApi {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getAllLaunches(): List<RocketLaunch> {
        return httpClient.get("https://api.spacexdata.com/v5/launches").body()
    }
    suspend fun downloadArticle(launch: RocketLaunch, client: SpaceXApi) {
        try {
            val url: String? = launch.links.article
            println("article for launch ${launch.flightNumber} : $url")

            val nameFile = url?.substringBeforeLast('/')
            val file = File("C:/users/MichaelBirepinte/Documents/TestKotlin/${nameFile?.substringAfterLast('/')}.html")
            if (!url.isNullOrEmpty()) {
                val urlToDl = Url(launch.links.article)
                println("Downloading.. $file")
                client.httpClient.get(urlToDl).bodyAsChannel().copyAndClose(file.writeChannel())
                println("Finished downloading... file is available here : " + file.getAbsolutePath())
            }
        } catch ( e: Exception) {
            println (e)
        }
    }
}

fun main() = runBlocking<Unit> {
    val service = SpaceXApi()
    val launches: List<RocketLaunch> = service.getAllLaunches()

    var i = 0
    for ((i, l) in launches.withIndex()) {
        println("Launch $i : $l")
        service.downloadArticle(l, service)
    }

    println("OPERATION FINISHED.")
}

@Serializable

data class RocketLaunch(

    @SerialName("flight_number")

    val flightNumber: Int,

    @SerialName("name")

    val missionName: String,

    @SerialName("date_utc")

    val launchDateUTC: String,

    @SerialName("details")

    val details: String?,

    @SerialName("success")

    val launchSuccess: Boolean?,

    @SerialName("links")

    val links: Links

) {

}


@Serializable

data class Links(

    @SerialName("patch")

    val patch: Patch?,

    @SerialName("article")

    val article: String?

)


@Serializable

data class Patch(

    @SerialName("small")

    val small: String?,

    @SerialName("large")

    val large: String?

)