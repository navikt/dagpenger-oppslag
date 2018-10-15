plugins {
    id("application")
    kotlin("jvm") version "1.2.51"
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
val cxfVersion = "2.5.1"

val wsdl2java by configurations.creating

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-gson:$fuelVersion")

    compile("io.ktor:ktor-server-netty:$ktorVersion")

    wsdl2java("org.apache.cxf:cxf-tools:$cxfVersion")
    wsdl2java("org.apache.cxf:cxf-tools-wsdlto-databinding-jaxb:$cxfVersion")
    wsdl2java("org.apache.cxf:cxf-tools-wsdlto-frontend-jaxws:$cxfVersion")

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

// WSDL generation

java {
    val mainJavaSourceSet: SourceDirectorySet = sourceSets.getByName("main").java
    mainJavaSourceSet.srcDir("$projectDir/build/generated-sources")
}

val wsdlDir = "$projectDir/src/main/resources/wsdl"

val wsdlsToGenerate = listOf("$wsdlDir/arena/Binding.wsdl", "$wsdlDir/gsak/Binding.wsdl", "$wsdlDir/person/Binding.wsdl")

val wsdl2javatask = tasks.create<JavaExec>("wsdl2Java") {
    description = "Runs org.apache.cxf.tools.wsdlto.WSDLToJava on wsdls in src/main/resources and places generated classes in build/generated-sources"
    main = "org.apache.cxf.tools.wsdlto.WSDLToJava"
    classpath = project.configurations.getByName("wsdl2java")
    
    inputs.files(file("src/main/resources/wsdl/arena/Binding.wsdl"))
    args(listOf("-d", "build/generated-sources", "src/main/resources/wsdl/arena/Binding.wsdl"))
}

tasks.getByName("compileJava").dependsOn(wsdl2javatask)