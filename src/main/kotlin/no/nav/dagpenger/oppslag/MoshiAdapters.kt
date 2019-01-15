package no.nav.dagpenger.oppslag

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.datatype.XMLGregorianCalendar

class XMLGregorianCalendarJsonAdapter {

    @ToJson
    fun toJson(xmlGregorianCalendar: XMLGregorianCalendar): String {

        val zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(xmlGregorianCalendar.toGregorianCalendar().timeInMillis),
            ZoneId.of("Europe/Oslo")
        )

        return zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    @FromJson
    fun fromJson(json: String): XMLGregorianCalendar {
        TODO("Not implemented")
    }
}

class BigDecimalJsonAdapter {

    @ToJson
    fun toJson(bigDecimal: BigDecimal): String {
        return bigDecimal.toString()
    }

    @FromJson
    fun fromJson(json: String): BigDecimal {
        return BigDecimal(json)
    }
}