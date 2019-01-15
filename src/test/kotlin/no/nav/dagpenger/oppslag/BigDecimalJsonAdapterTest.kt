package no.nav.dagpenger.oppslag

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

internal class BigDecimalJsonAdapterTest {

    @ParameterizedTest
    @CsvSource(
        "1.00, 1.00",
        "22.657, 22.657",
        "3, 3"
    )
    fun toJson(input: BigDecimal, expected: String) {
        Assertions.assertEquals(BigDecimalJsonAdapter().toJson(input), expected)
    }

    @ParameterizedTest
    @CsvSource(
        "1.00, 1.00",
        "22.657, 22.657",
        "3, 3"
    )
    fun fromJson(input: String, expected: BigDecimal) {
        Assertions.assertEquals(BigDecimalJsonAdapter().fromJson(input), expected)
    }
}