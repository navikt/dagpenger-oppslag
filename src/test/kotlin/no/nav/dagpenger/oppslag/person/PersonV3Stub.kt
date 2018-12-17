package no.nav.dagpenger.oppslag.person

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Land
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse

class PersonV3Stub : PersonV3 {

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
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}