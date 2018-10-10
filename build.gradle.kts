import no.nils.wsdl2java.Wsdl2JavaTask

plugins {
    id("application")
    kotlin("jvm") version "1.2.51"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.palantir.docker") version "0.20.1"
    id("com.palantir.git-version") version "0.11.0"
    id("com.adarshr.test-logger") version "1.5.0"
    id("no.nils.wsdl2java") version "0.10"
}

apply {
    plugin("com.diffplug.gradle.spotless")
    plugin("com.adarshr.test-logger")
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "http://packages.confluent.io/maven/")
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://repo.adeo.no/repository/maven-snapshots/")
    maven(url = "https://repo.adeo.no/repository/maven-releases/")
}

val gitVersion: groovy.lang.Closure<Any> by extra
version = gitVersion()
group = "no.nav.dagpenger"

application {
    applicationName = "dagpenger-oppslag"
    mainClassName = "no.nav.dagpenger.oppslag.Oppslag"
}

docker {
    name = "repo.adeo.no:5443/navikt/${application.applicationName}"
    buildArgs(mapOf(
        "APP_NAME" to application.applicationName,
        "DIST_TAR" to "${application.applicationName}-${project.version}"
    ))
    files(tasks.findByName("distTar")?.outputs)
    pull(true)
    tags(project.version.toString())
}

val kotlinLoggingVersion = "1.4.9"
val fuelVersion = "1.15.0"
val confluentVersion = "4.1.2"
val kafkaVersion = "2.0.0"
val ktorVersion = "0.9.5"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")

    compile("io.ktor:ktor-server-netty:$ktorVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.12")
    testImplementation("com.github.tomakehurst:wiremock:2.18.0")
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

project.afterEvaluate {
    tasks.create<Wsdl2JavaTask>("wsdl") {
        generatedWsdlDir = File("build/generated-sources")
        wsdlsToGenerate = arrayListOf(
                arrayListOf("src/main/resources/wsdl/arena/Binding.wsdl"),
                arrayListOf("src/main/resources/wsdl/person/Binding.wsdl"))
        wsdl2java()
    }
}