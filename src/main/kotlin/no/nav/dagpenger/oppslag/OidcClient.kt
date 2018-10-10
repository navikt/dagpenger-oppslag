package no.nav.dagpenger.oppslag

interface OidcClient {
    fun oidcToken(): OidcToken
}
