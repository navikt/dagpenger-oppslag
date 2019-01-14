package no.nav.dagpenger.oppslag.inntekt

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.prometheus.client.CollectorRegistry
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.samlAssertionResponse
import no.nav.dagpenger.oppslag.stsStub
import no.nav.dagpenger.oppslag.withCallId
import no.nav.dagpenger.oppslag.withSamlAssertion
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InntektComponentTest {

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

    @AfterEach
    fun `clear prometheus registry`() {
        CollectorRegistry.defaultRegistry.clear()
    }

    @Test
    fun `that response is json`() {
        val jwtStub = JwtStub("test issuer")
        val token = jwtStub.createTokenFor("srvdp-inntekt-api")

        WireMock.stubFor(stsStub("stsUsername", "stsPassword")
            .willReturn(samlAssertionResponse("testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                "digestValue", "signatureValue", "certificateValue")))

        WireMock.stubFor(hentInntektListeBolkStub("12345678", "2017-12Z", "2018-01Z")
            .withSamlAssertion("testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                "digestValue", "signatureValue", "certificateValue")
            .withCallId("Sett inn call id her")
            .willReturn(WireMock.okXml(hentInntektListeBolk_response)))

        val env = Environment(mapOf(
            "SECURITYTOKENSERVICE_URL" to server.baseUrl().plus("/sts"),
            "SRVDAGPENGER_OPPSLAG_USERNAME" to "stsUsername",
            "SRVDAGPENGER_OPPSLAG_PASSWORD" to "stsPassword",
            "INNTEKT_ENDPOINTURL" to server.baseUrl().plus("/inntekt"),
            "JWT_ISSUER" to "test issuer",
            "ALLOW_INSECURE_SOAP_REQUESTS" to "true"
        ))

        withTestApplication({ oppslag(env, jwtStub.stubbedJwkProvider()) }) {
            handleRequest(HttpMethod.Post, "/api/inntekt/inntekt-liste") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody("{\"aktorid\": \"12345678\"}")
            }.apply {
                Assertions.assertEquals(200, response.status()?.value)
            }
        }
    }
}

private val hentInntektListeBolk_response = """
<?xml version="1.0" encoding="UTF-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <soap:Body>
      <ns2:hentInntektListeBolkResponse xmlns:ns2="http://nav.no/tjeneste/virksomhet/inntekt/v3">
         <response>
            <arbeidsInntektIdentListe>
               <arbeidsInntektMaaned>
                  <aarMaaned>2017-12+01:00</aarMaaned>
                  <arbeidsInntektInformasjon>
                     <inntektListe xsi:type="ns4:Loennsinntekt" xmlns:ns4="http://nav.no/tjeneste/virksomhet/inntekt/v3/informasjon/inntekt" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <beloep>40000</beloep>
                        <fordel>kontantytelse</fordel>
                        <inntektskilde>A-ordningen</inntektskilde>
                        <inntektsperiodetype>Maaned</inntektsperiodetype>
                        <inntektsstatus>LoependeInnrapportert</inntektsstatus>
                        <levereringstidspunkt>2018-12-05T09:50:10.777+01:00</levereringstidspunkt>
                        <utbetaltIPeriode>2017-12</utbetaltIPeriode>
                        <opplysningspliktig xsi:type="ns4:Organisasjon">
                           <orgnummer>973861778</orgnummer>
                        </opplysningspliktig>
                        <virksomhet xsi:type="ns4:Organisasjon">
                           <orgnummer>973861778</orgnummer>
                        </virksomhet>
                        <inntektsmottaker xsi:type="ns4:AktoerId">
                           <aktoerId>12345678</aktoerId>
                        </inntektsmottaker>
                        <inngaarIGrunnlagForTrekk>true</inngaarIGrunnlagForTrekk>
                        <utloeserArbeidsgiveravgift>true</utloeserArbeidsgiveravgift>
                        <informasjonsstatus>InngaarAlltid</informasjonsstatus>
                        <beskrivelse>fastloenn</beskrivelse>
                     </inntektListe>
                  </arbeidsInntektInformasjon>
               </arbeidsInntektMaaned>
               <arbeidsInntektMaaned>
                  <aarMaaned>2018-01+01:00</aarMaaned>
                  <arbeidsInntektInformasjon>
                     <inntektListe xsi:type="ns4:Loennsinntekt" xmlns:ns4="http://nav.no/tjeneste/virksomhet/inntekt/v3/informasjon/inntekt" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <beloep>40000</beloep>
                        <fordel>kontantytelse</fordel>
                        <inntektskilde>A-ordningen</inntektskilde>
                        <inntektsperiodetype>Maaned</inntektsperiodetype>
                        <inntektsstatus>LoependeInnrapportert</inntektsstatus>
                        <levereringstidspunkt>2018-12-05T09:50:10.777+01:00</levereringstidspunkt>
                        <utbetaltIPeriode>2018-01</utbetaltIPeriode>
                        <opplysningspliktig xsi:type="ns4:Organisasjon">
                           <orgnummer>973861778</orgnummer>
                        </opplysningspliktig>
                        <virksomhet xsi:type="ns4:Organisasjon">
                           <orgnummer>973861778</orgnummer>
                        </virksomhet>
                        <inntektsmottaker xsi:type="ns4:AktoerId">
                           <aktoerId>12345678</aktoerId>
                        </inntektsmottaker>
                        <inngaarIGrunnlagForTrekk>true</inngaarIGrunnlagForTrekk>
                        <utloeserArbeidsgiveravgift>true</utloeserArbeidsgiveravgift>
                        <informasjonsstatus>InngaarAlltid</informasjonsstatus>
                        <beskrivelse>fastloenn</beskrivelse>
                     </inntektListe>
                  </arbeidsInntektInformasjon>
               </arbeidsInntektMaaned>
               <ident xsi:type="ns4:AktoerId" xmlns:ns4="http://nav.no/tjeneste/virksomhet/inntekt/v3/informasjon/inntekt" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                  <aktoerId>12345678</aktoerId>
               </ident>
            </arbeidsInntektIdentListe>
         </response>
      </ns2:hentInntektListeBolkResponse>
   </soap:Body>
</soap:Envelope>
""".trimIndent()