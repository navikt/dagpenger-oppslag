package no.nav.dagpenger.oppslag

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.xml.datatype.DatatypeFactory

internal class XMLGregorianCalendarJsonAdapterTest {

    @Test
    fun toJson() {
        val date = DatatypeFactory.newInstance().newXMLGregorianCalendar(2018, 1, 1, 22, 43, 11, 0, 0)
        Assertions.assertEquals(
            "2018-01-01T23:43:11+01:00[Europe/Oslo]",
            XMLGregorianCalendarJsonAdapter().toJson(date)
        )
    }
}