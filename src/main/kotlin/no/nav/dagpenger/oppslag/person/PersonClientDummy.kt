package no.nav.dagpenger.oppslag.person

class PersonClientDummy : PersonClient {
    override fun getGeografiskTilknytning(fødelsnummer: String): String {
        return "0000"
    }
}