package no.nav.dagpenger.oppslag.ws.brreg.enhetsregister

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EnhetsRegisterClientTest {
    companion object {
        val server: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun start() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            server.stop()
        }
    }

    @BeforeEach
    fun configure() {
        WireMock.configureFor(server.port())
    }

    @Test
    fun `Successful fetch of details`() {
        val testOrgId = "974760673"
        val testName = "REGISTERENHETEN I BRØNNØYSUND"

        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//enheter/$testOrgId"))
                .willReturn(WireMock.aResponse().withBody(validJsonBodyWithNorskOrg))
        )
        val enhetsRegisteretHttpClient = EnhetsRegisteretHttpClient(server.url(""))
        val enhetsNavn = enhetsRegisteretHttpClient.getOrgName(testOrgId)
        Assertions.assertEquals(testName, enhetsNavn)
    }

    @Test
    fun `Empty string if brreg returns 404`() {
        val testOrgId = "974760673"

        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//enheter/$testOrgId"))
                .willReturn(WireMock.notFound())
        )

        val enhetsRegisteretHttpclient = EnhetsRegisteretHttpClient(server.url(""))
        Assertions.assertThrows(EnhetsRegisteretHttpClientException::class.java) {
            enhetsRegisteretHttpclient.getOrgName(testOrgId)
        }
    }

    val validJsonBodyWithNorskOrg = """
        {"organisasjonsnummer":"974760673",
        "navn":"REGISTERENHETEN I BRØNNØYSUND",
        "organisasjonsform":{"kode":"ORGL",
        "beskrivelse":"Organisasjonsledd",
        "_links":{
            "self":{
                "href":"https://data.brreg.no/enhetsregisteret/api/organisasjonsformer/ORGL"
            }
        }},"hjemmeside":"www.brreg.no",
        "postadresse":{
            "land":"Norge",
            "landkode":"NO",
            "postnummer":"8910",
            "poststed":"BRØNNØYSUND",
            "adresse":["Postboks 900"],
            "kommune":"BRØNNØY",
            "kommunenummer":"1813"
        },"registreringsdatoEnhetsregisteret":"1995-08-09",
          "registrertIMvaregisteret":false,
          "naeringskode1":{
            "beskrivelse":"Generell offentlig administrasjon","kode":"84.110"
            },"antallAnsatte":533,"overordnetEnhet":"912660680",
            "forretningsadresse":{
            "land":"Norge","landkode":"NO","postnummer":"8900","poststed":"BRØNNØYSUND",
            "adresse":["Havnegata 48"],"kommune":"BRØNNØY","kommunenummer":"1813"
            },"institusjonellSektorkode":{"kode":"6100","beskrivelse":"Statsforvaltningen"},
            "registrertIForetaksregisteret":false,"registrertIStiftelsesregisteret":false,
            "registrertIFrivillighetsregisteret":false,"konkurs":false,"underAvvikling":false,
            "underTvangsavviklingEllerTvangsopplosning":false,"maalform":"Bokmål",
            "_links":{"self":{"href":"https://data.brreg.no/enhetsregisteret/api/enheter/974760673"},
            "overordnetEnhet":{"href":"https://data.brreg.no/enhetsregisteret/api/enheter/912660680"}}
            }""".trimIndent()
}