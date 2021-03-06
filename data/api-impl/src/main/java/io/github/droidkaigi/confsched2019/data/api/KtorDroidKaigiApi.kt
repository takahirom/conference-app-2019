package io.github.droidkaigi.confsched2019.data.api

import io.github.droidkaigi.confsched2019.data.api.response.Response
import io.github.droidkaigi.confsched2019.data.api.response.ResponseImpl
import io.github.droidkaigi.confsched2019.data.api.response.SponsorResponse
import io.github.droidkaigi.confsched2019.data.api.response.SponsorResponseImpl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import javax.inject.Inject
import javax.inject.Named

class KtorDroidKaigiApi @Inject constructor(
    val httpClient: HttpClient,
    @Named("apiEndpoint") val apiEndpoint: String
) : DroidKaigiApi {
    override suspend fun getSponsors(): SponsorResponse {
        return httpClient.get<SponsorResponseImpl> {
            url("$apiEndpoint/sponsors")
        }
    }

    override suspend fun getSessions(): Response {
        return httpClient.get<ResponseImpl> {
            url("$apiEndpoint/timetable")
        }
    }
}
