package no.nav.dagpenger.oppslag.person

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Land
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentEkteskapshistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentEkteskapshistorikkResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonerMedSammeAdresseRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonerMedSammeAdresseResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentVergeRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentVergeResponse

class PersonV3Stub : PersonV3 {
    override fun hentPersonhistorikk(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentVerge(request: HentVergeRequest?): HentVergeResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentEkteskapshistorikk(request: HentEkteskapshistorikkRequest?): HentEkteskapshistorikkResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPersonerMedSammeAdresse(request: HentPersonerMedSammeAdresseRequest?): HentPersonerMedSammeAdresseResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentGeografiskTilknytning(request: HentGeografiskTilknytningRequest?): HentGeografiskTilknytningResponse {
        val resp = HentGeografiskTilknytningResponse()
        resp.geografiskTilknytning = Land().apply { geografiskTilknytning = "et sted" }
        resp.diskresjonskode = Diskresjonskoder().apply { value = "2" }
        return resp
    }

    override fun ping() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPersonnavnBolk(request: HentPersonnavnBolkRequest?): HentPersonnavnBolkResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentSikkerhetstiltak(request: HentSikkerhetstiltakRequest?): HentSikkerhetstiltakResponse {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun hentPerson(request: HentPersonRequest?): HentPersonResponse {
        val resp = HentPersonResponse()
        resp.person = Person()
        resp.person.personnavn = Personnavn().apply {
            etternavn = "etternavntest"
            fornavn = "fornavntest"
            mellomnavn = "mellomnavntest"
            sammensattNavn = "sammensattnavntest"
        }
        return resp
    }
}
