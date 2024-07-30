plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.emojibot"
version = "1.1"

repositories {
    mavenCentral() ;
    maven { url = uri("https://jitpack.io") }
    
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

    // mongodb for database
    implementation("org.mongodb:mongodb-driver-sync:5.1.2")

    // webhooks for logs
    implementation("club.minnced:discord-webhooks:0.8.4")

    // top.gg 
    implementation("org.discordbots:DBL-Java-Library:2.1.2")

}

tasks.test {
    useJUnitPlatform()
}

// fat Jar file to run the bot (includes all the dependencies)
tasks.shadowJar {
    archiveBaseName.set("emojibot")
    archiveClassifier.set("") // Removes the '-all' suffix
    archiveVersion.set("1.1") // Version
}

application {
    // Specify the main class of your application
    mainClass.set("com.emojibot.Bot") // Adjust this to your actual main class path
}