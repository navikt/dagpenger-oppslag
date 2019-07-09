import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        jcenter()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

application {
    applicationName = "dagpenger-oppslag"
    mainClassName = "no.nav.dagpenger.oppslag.OppslagAppKt"
}

val cxfVersion = "3.3.1"
val tjenestespesifikasjonerVersion = "1.2019.01.16-21.19-afc54bed6f85"

fun tjenestespesifikasjon(name: String) = "no.nav.tjenestespesifikasjoner:$name:$tjenestespesifikasjonerVersion"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Fuel.fuel)
    implementation(Fuel.fuelMoshi)

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation(Moshi.moshiKtor)

    implementation(Ktor.server)
    implementation(Ktor.serverNetty)
    implementation(Ktor.auth)
    implementation(Ktor.authJwt)
    implementation(Ktor.locations)
    implementation(Ktor.micrometerMetrics)

    implementation(Micrometer.prometheusRegistry)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)

    implementation(Dagpenger.Biblioteker.ktorUtils)
    implementation(Dagpenger.Biblioteker.stsKlient)

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.Logstash.logstashLayout)
    implementation(Kotlin.Logging.kotlinLogging)

    // Soap stuff
    implementation("javax.xml.ws:jaxws-api:2.3.1")
    implementation("com.sun.xml.ws:jaxws-tools:2.3.0.2")

    compile(tjenestespesifikasjon("person-v3-tjenestespesifikasjon"))

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("javax.activation:activation:1.1.1")
    implementation("no.nav.helse:cxf-prometheus-metrics:dd7d125")
    testImplementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    // Soap stuff end

    testImplementation(kotlin("test"))
    testImplementation(Ktor.ktorTest)
    testImplementation(Junit5.api)
    testImplementation(Junit5.kotlinRunner)
    testRuntimeOnly(Junit5.engine)
    testImplementation(Wiremock.standalone)
    testImplementation(Mockk.mockk)
}

spotless {
    kotlin {
        ktlint(Klint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint(Klint.version)
    }
}

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    mainJavaSourceSet.srcDir("$projectDir/build/generated-sources")
}

val wsdlDir = "$projectDir/src/main/resources/wsdl"
val wsdlsToGenerate = listOf(
        "$wsdlDir/arena/Binding.wsdl",
        "$wsdlDir/arbeidsfordeling/Binding.wsdl",
        "$wsdlDir/hentsak/arenaSakVedtakService.wsdl")

val generatedDir = "$projectDir/build/generated-sources"

tasks.withType<ShadowJar> {
    mergeServiceFiles()

    // Make sure the cxf service files are handled correctly so that the SOAP services work.
    // Ref https://stackoverflow.com/questions/45005287/serviceconstructionexception-when-creating-a-cxf-web-service-client-scalajava
    transform(ServiceFileTransformer::class.java) {
        setPath("META-INF/cxf")
        include("bus-extensions.txt")
    }
}

tasks {
    register("wsimport") {
        inputs.files(wsdlsToGenerate)
        outputs.dir(generatedDir)

        group = "other"
        doLast {
            mkdir(generatedDir)
            wsdlsToGenerate.forEach {
                ant.withGroovyBuilder {
                    "taskdef"("name" to "wsimport", "classname" to "com.sun.tools.ws.ant.WsImport", "classpath" to sourceSets.getAt("main").runtimeClasspath.asPath)
                    "wsimport"("wsdl" to it, "sourcedestdir" to generatedDir, "xnocompile" to true) {}
                }
            }
        }
    }
}

tasks.getByName("compileKotlin").dependsOn("wsimport")

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
