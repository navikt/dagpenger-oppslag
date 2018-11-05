package no.nav

import no.nav.arena.services.lib.sakvedtak.SaksInfo
import no.nav.dagpenger.oppslag.arena.findNewestActiveSak
import org.junit.Before
import org.junit.Test
import java.util.*
import javax.xml.datatype.DatatypeFactory
import kotlin.test.assertEquals

class ArenaControllerTest {

    lateinit var sak1: SaksInfo
    lateinit var sak2: SaksInfo
    lateinit var sak3: SaksInfo

    @Before
    fun setUp() {
        sak1 = SaksInfo().apply {
            saksId = "10"
            sakstatus = "AKTIV"
            sakOpprettet = GregorianCalendar(2018, 10, 10).let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
        }

        sak2 = SaksInfo().apply {
            saksId = "11"
            sakstatus = "AKTIV"
            sakOpprettet = GregorianCalendar(2018, 10, 12).let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
        }

        sak3 = SaksInfo().apply {
            saksId = "12"
            sakstatus = "INAKTIV"
            sakOpprettet = GregorianCalendar(2018, 11, 1).let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
        }
    }

    @Test
    fun findLatestCreatedSakTest() {

        val saksListe =

        assertEquals(findNewestActiveSak(listOf(sak1, sak2, sak3)), sak2)
    }

    @Test
    fun findLatestCreatedSakTest_returnsNullOnEmptyList() {
        assertEquals(findNewestActiveSak(listOf()), null)

        assertEquals(findNewestActiveSak(listOf(sak3)), null)
    }
}