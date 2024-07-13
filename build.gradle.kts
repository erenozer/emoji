plugins {
    id("java")
}

group = "com.emojibot"
version = "1.0"

repositories {
    mavenCentral()
}


dependencies {
    // JDA 5.0.0
    implementation("net.dv8tion:JDA:5.0.0") {
        exclude(module="opus-java")
    }

    // dotenv for config
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    // logging for jda
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // webhooks for logs
    implementation("club.minnced:discord-webhooks:0.8.4")

}

tasks.test {
    useJUnitPlatform()
}