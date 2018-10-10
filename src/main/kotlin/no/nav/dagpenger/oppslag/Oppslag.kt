package no.nav.dagpenger.oppslag

import mu.KotlinLogging
import no.nav.dagpenger.oppslag.arena.ArenaClient
import no.nav.dagpenger.oppslag.arena.ArenaClientDummy
import no.nav.dagpenger.oppslag.person.PersonClient
import no.nav.dagpenger.oppslag.person.PersonClientDummy

private val LOGGER = KotlinLogging.logger {}

class Oppslag(private val arenaClient: ArenaClient, private val personClient: PersonClient) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val oppslag = Oppslag(ArenaClientDummy(), PersonClientDummy())
        }
    }
}
