buildscript {
    extra["tomcatPluginVersion"] = "2.5"
    val tomcatPluginVersion: String by extra

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.bmuschko:gradle-tomcat-plugin:$tomcatPluginVersion")
    }
}

plugins {
    java
    kotlin("jvm") version "1.4.21"
    application
    kotlin("plugin.serialization") version "1.4.21"
    id("com.bmuschko.tomcat") version "2.5"
    war
}

group = "org.example"
version = "1.0-SNAPSHOT"

val coroutinesVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val koinVersion: String by project
val tomcatVersion: String by project

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-servlet:$ktorVersion")

    // DI
    implementation("org.koin:koin-core:$koinVersion")
    implementation("org.koin:koin-ktor:$koinVersion")

    // Logs
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    // DB
    implementation("com.h2database", "h2", "1.3.148")
    // ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    tomcat("org.apache.tomcat.embed:tomcat-embed-core:$tomcatVersion")
    tomcat("org.apache.tomcat.embed:tomcat-embed-jasper:$tomcatVersion")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configure<com.bmuschko.gradle.tomcat.extension.TomcatPluginExtension> {
    contextPath = "/"
    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
    ajpProtocol = "org.apache.coyote.ajp.AjpNio2Protocol"
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
tasks.test {
    useJUnitPlatform()
}
