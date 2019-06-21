package no.nav.dagpenger.oppslag.ws.brreg.enhetsregister

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.slf4j.LoggerFactory

fun Route.enhetRegister(enhetRegisterClient: EnhetsRegisteretHttpClient) {
    get("api/organisasjon/{orgid}") {
        try {
            call.parameters["orgid"]?.toLong()?.let { orgId ->
                try {
                    val orgName = enhetRegisterClient.getOrgName(orgId.toString())
                    call.respond(HttpStatusCode.OK, Organisation(orgId, orgName))
                } catch (f: EnhetsRegisteretHttpClientException) {
                    logger.warn("Failed to talk to Enhetsregisteret", f)
                    call.respond(HttpStatusCode.fromValue(f.status))
                }
            } ?: call.respond(HttpStatusCode.NotAcceptable, "Kunne ikke lese organisasjons nummer")
        } catch (nfe: NumberFormatException) {
            call.respond(HttpStatusCode.NotAcceptable, "Organisasjonsnummer må kunne leses som ett tall")
        }
    }
}

data class Organisation(val orgNr: Long, val navn: String)

val logger = LoggerFactory.getLogger("EnhetRegister")
