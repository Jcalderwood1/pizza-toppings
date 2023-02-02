import de.qualersoft.jmeter.gradleplugin.task.JMeterRunTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("de.qualersoft.jmeter") version "2.4.0"
	id("org.springframework.boot") version "3.0.2"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
}

group = "com.jordancalderwood"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks {
	register<JMeterRunTask>("runJMeter") {
		jmxFile.set("Test.jmx")
		dependsOn("copyTestData")
	}
}

tasks.register<Copy>("copyTestData") {
	from("src/test/jmeter/toppings.csv")
	into("build/jmeter/bin/")
}