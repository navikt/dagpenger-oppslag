package no.nav.dagpenger.oppslag.ws.brreg.enhetsregister

import com.github.kittinunf.fuel.httpGet
import no.nav.dagpenger.oppslag.responseObject

class EnhetsRegisteretHttpClient(private val enhetsRegisteretUrl: String) {

    fun getOrgName(orgNr: String): String {
        val url = "$enhetsRegisteretUrl/enheter/$orgNr"

        val (_, _, result) = with(url.httpGet()) {
            responseObject<EnhetResponse>()
        }
        return result.fold(
            { success ->
                success.navn
            },
            { error ->
                throw EnhetsRegisteretHttpClientException(
                    error.response.statusCode,
                    "Failed to fetch organisation name. Response message: ${error.response.responseMessage}",
                    error
                )
            }
        )
    }
}

data class EnhetResponse(val navn: String)

class EnhetsRegisteretHttpClientException(val status: Int, override val message: String, t: Throwable) : RuntimeException(message, t)
