import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

plugins {
    id("application")
    kotlin("jvm") version "1.3.40"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://dl.bintray.com/kittinunf/maven")
    maven("https://jitpack.io")
}

application {
    applicationName = "dagpenger-oppslag"
    mainClassName = "no.nav.dagpenger.oppslag.OppslagAppKt"
}

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
}

val confluentVersion = "5.2.1"
val cxfVersion = "3.3.1"
val dpBibliotekerVersion = "2019.06.19-09.38.5466af242e44"
val fuelVersion = "2.1.0"
val kafkaVersion = "2.2.1"
val kotlinLoggingVersion = "1.6.22"
val ktorVersion = "1.2.0"
val moshiVersion = "1.8.0"
val prometheusVersion = "0.6.0"
val junitJupiterVersion = "5.4.1"
val log4j2Version = "2.11.1"
val mockkVersion = "1.9.1"
val tjenestespesifikasjonerVersion = "1.2019.01.16-21.19-afc54bed6f85"

fun tjenestespesifikasjon(name: String) = "no.nav.tjenestespesifikasjoner:$name:$tjenestespesifikasjonerVersion"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-moshi:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")

    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")

    implementation("com.sun.xml.ws:jaxws-tools:2.3.0.2")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_log4j2:$prometheusVersion")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    compile(tjenestespesifikasjon("person-v3-tjenestespesifikasjon"))

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("javax.activation:activation:1.1.1")
    implementation("com.github.navikt.dp-biblioteker:sts-klient:$dpBibliotekerVersion")
    compile("no.nav.helse:cxf-prometheus-metrics:dd7d125")

    testCompile("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")

    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-auth-jwt:$ktorVersion")
    compile("io.ktor:ktor-auth:$ktorVersion")
    compile("com.squareup.okio:okio:2.1.0")
    compile("com.ryanharter.ktor:ktor-moshi:1.0.1")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.1.4")

    implementation("com.sun.xml.ws:jaxws-tools:2.3.0.2")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    implementation("com.vlkan.log4j2:log4j2-logstash-layout-fatjar:0.15")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("com.github.tomakehurst:wiremock:2.19.0")
    testImplementation("io.mockk:mockk:$mockkVersion")

    testCompile("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty") // conflicts with WireMock
    }
}

spotless {
    kotlin {
        ktlint("0.31.0")
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint("0.31.0")
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
