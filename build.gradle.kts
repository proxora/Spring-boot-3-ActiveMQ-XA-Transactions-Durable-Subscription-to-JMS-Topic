plugins {
	java
	// only Spring Boot 3.3 because of atomikos issue with Spring Boot 3.4: https://github.com/atomikos/transactions-essentials/issues/234
	//id("org.springframework.boot") version "3.3.11"
	id("org.springframework.boot") version "3.4.5"
	//id("org.springframework.boot") version "3.5.0-RC1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-activemq")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.apache.commons:commons-lang3:3.17.0")


	compileOnly("org.projectlombok:lombok")

	// we want a JTA transaction manager to always use XA Transactions when using messaging!
    //runtimeOnly("dev.snowdrop:narayana-spring-boot-starter:3.4.0")
	// atomikos JTA Transaction Manager
	compileOnly("com.atomikos:transactions-spring-boot3-starter:6.0.0")
	runtimeOnly("com.atomikos:transactions-spring-boot3-starter:6.0.0")

	// MessagingHub as a more sophisticated JMS connection management
	//runtimeOnly("org.messaginghub:pooled-jms")

	runtimeOnly("com.h2database:h2")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
