plugins {
    id("java")
}

group = "com.emojibot"
version = "1.0"

repositories {
    mavenCentral()
}


dependencies {
    // JDA 5.0.0-beta.24
    implementation("net.dv8tion:JDA:5.0.0-beta.24")

    // dotenv for config
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
}

tasks.test {
    useJUnitPlatform()
}