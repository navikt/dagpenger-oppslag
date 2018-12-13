plugins {
    id("application")
    kotlin("jvm") version "1.3.10"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.palantir.docker") version "0.20.1"
    id("com.palantir.git-version") version "0.11.0"
    id("com.adarshr.test-logger") version "1.5.0"
}

apply {
    plugin("com.diffplug.gradle.spotless")
    plugin("com.adarshr.test-logger")
}

repositories {
    mavenCentral()
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

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
}

docker {
    name = "repo.adeo.no:5443/${application.applicationName}"
    buildArgs(mapOf(
            "APP_NAME" to application.applicationName,
            "DIST_TAR" to "${application.applicationName}-${project.version}"
    ))
    files(tasks.findByName("distTar")?.outputs)
    pull(true)
    tags(project.version.toString())
}

val confluentVersion = "4.1.2"
val cxfVersion = "3.2.6"
val fuelVersion = "1.15.0"
val kafkaVersion = "2.0.0"
val kotlinLoggingVersion = "1.4.9"
val ktorVersion = "1.0.0"
val moshiVersion = "1.8.0"
val prometheusVersion = "0.5.0"

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
    implementation("org.apache.cxf:cxf-rt-transports-http-jetty:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.slf4j:slf4j-simple:1.6.1")

    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-auth-jwt:$ktorVersion")
    compile("com.ryanharter.ktor:ktor-moshi:1.0.0")
    compile("com.squareup.okio:okio:2.1.0")
    compile("io.ktor:ktor-server-netty:$ktorVersion")

    testImplementation("com.github.tomakehurst:wiremock:2.19.0")
    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
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
        "$wsdlDir/hentsak/arenaSakVedtakService.wsdl")
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
