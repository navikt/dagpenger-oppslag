import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("application")
    kotlin("jvm") version "1.3.10"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://dl.bintray.com/kittinunf/maven")
}

application {
    applicationName = "dagpenger-oppslag"
    mainClassName = "no.nav.dagpenger.oppslag.Oppslag"
}

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
}

val confluentVersion = "4.1.2"
val cxfVersion = "3.2.7"
val fuelVersion = "1.15.0"
val kafkaVersion = "2.0.0"
val kotlinLoggingVersion = "1.4.9"
val ktorVersion = "1.0.0"
val moshiVersion = "1.8.0"
val prometheusVersion = "0.5.0"
val junitJupiterVersion = "5.3.1"
val log4j2Version = "2.11.1"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")

    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")

    implementation("com.sun.xml.ws:jaxws-tools:2.3.0.2")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("javax.xml.ws:jaxws-api:2.3.1")

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    testCompile("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")

    implementation("org.slf4j:slf4j-simple:1.6.1")

    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-auth-jwt:$ktorVersion")
    compile("com.ryanharter.ktor:ktor-moshi:1.0.1")
    compile("com.squareup.okio:okio:2.1.0")
    compile("com.ryanharter.ktor:ktor-moshi:1.0.1")

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

    testCompile("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty") // conflicts with WireMock
    }
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        target("*.gradle.kts", "additionalScripts/*.gradle.kts")
        ktlint()
    }
}

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    mainJavaSourceSet.srcDir("$projectDir/build/generated-sources")
}

val wsdlDir = "$projectDir/src/main/resources/wsdl"
val wsdlsToGenerate = listOf(
        "$wsdlDir/arena/Binding.wsdl",
        "$wsdlDir/inngaaendejournal/Binding.wsdl",
        "$wsdlDir/person/Binding.wsdl",
        "$wsdlDir/arbeidsfordeling/Binding.wsdl",
        "$wsdlDir/hentsak/arenaSakVedtakService.wsdl",
        "$wsdlDir/inntektskomponenten.v3/Binding.wsdl")
val generatedDir = "$projectDir/build/generated-sources"

tasks {
    register("wsimport") {
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
