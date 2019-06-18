package no.nav.dagpenger.oppslag.ws.aktor

import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import no.nav.dagpenger.oidc.OidcClient
import no.nav.dagpenger.oppslag.moshiInstance

class AktorRegisterHttpClient(
    val baseUrl: String,
    private val oidcClient: OidcClient
) {

    private val jsonResponseAdapter: JsonAdapter<Map<String, IdentResponse>> = moshiInstance.adapter(
        Types.newParameterizedType(Map::class.java, String::class.java, IdentResponse::class.java)
    )

    private fun gjeldendeIdenter(ident: String): List<Ident> {
        val url = "$baseUrl/v1/identer?gjeldende=true"

        val (_, _, result) = with(
            url.httpGet().authentication()
                .bearer(oidcClient.oidcToken().access_token)
                .header(
                    mapOf(
                        "Accept" to "application/json",
                        "Nav-Call-Id" to "dagpenger",
                        "Nav-Consumer-Id" to "dagpenger-oppslag",
                        "Nav-Personidenter" to ident
                    )
                )
        ) {
            responseObject(moshiDeserializerOf(jsonResponseAdapter))
        }
        return result.fold(
            { success -> success[ident]?.identer ?: emptyList() },
            { error ->
                throw AktorregisterHttpClientException(
                    error.response.statusCode,
                    "Failed to fetch name. Response message: ${error.response.responseMessage}. Problem message: ${error.message}"
                )
            }
        )
    }

    private fun gjeldendeIdent(ident: String, gruppe: IdentGruppe): String? {
        return gjeldendeIdenter(ident).firstOrNull {
            it.identgruppe == gruppe
        }?.ident
    }

    fun gjeldendeNorskIdent(ident: String): String? {
        return gjeldendeIdent(ident, IdentGruppe.NorskIdent)
    }
}

data class IdentResponse(val identer: List<Ident>, val feilmelding: String?)

enum class IdentGruppe {
    AktoerId, NorskIdent
}

data class Ident(val ident: String, val identgruppe: IdentGruppe)

class AktorregisterHttpClientException(val status: Int, override val message: String) : RuntimeException(message)