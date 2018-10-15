package no.nav.dagpenger.oppslag.person

interface PersonClient {
    fun getGeografiskTilknytning(fødelsnummer: String): String
}