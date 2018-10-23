plugins {
    id("application")
    kotlin("jvm") version "1.2.51"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.palantir.docker") version "0.20.1"
    id("com.palantir.git-version") version "0.11.0"
    id("com.adarshr.test-logger") version "1.5.0"
    id("uk.co.boothen.gradle.wsimport") version "0.3.4"
}

buildscript {
    repositories {
        maven("https://repo.adeo.no/repository/maven-central")
    }
}

apply {
    plugin("com.diffplug.gradle.spotless")
    plugin("com.adarshr.test-logger")
}

repositories {
    maven("https://repo.adeo.no/repository/maven-central")
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://dl.bintray.com/kittinunf/maven")
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
val cxfVersion = "3.2.6"

val jaxws by configurations.creating

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")

    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http-jetty:$cxfVersion")

    implementation("org.slf4j:slf4j-simple:1.6.1")

    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-gson:$ktorVersion")

    jaxws("com.sun.xml.ws:jaxws-tools:2.1.4")

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

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    mainJavaSourceSet.srcDir("$projectDir/build/generated-sources")
}

val wsimport = tasks.create<uk.co.boothen.gradle.wsimport.WsImport>("wsimport") {
    setGeneratedSourceRoot("generated-sources")
    wsdl("person/Binding.wsdl")
    wsdl("arena/Binding.wsdl")
    wsdl("arbeidsfordeling/Binding.wsdl")
    wsdl("gsak/SakV2.wsdl")
}

tasks.getByName("compileKotlin").dependsOn(wsimport)