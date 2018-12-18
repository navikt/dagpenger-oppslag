package no.nav.dagpenger.oppslag

sealed class OppslagResult

data class Success<T>(val data: T?) : OppslagResult()

data class Failure(val errors: List<String>) : OppslagResult()
