import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.10"
  id("fabric-loom") version "0.2.2-SNAPSHOT"
}

base {
  archivesBaseName = "retrocomputers"
}

group = "therealfarfetchd.retrocomputers"
version = "1.0.0"

java {
  sourceCompatibility = VERSION_1_8
  targetCompatibility = VERSION_1_8
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
}

minecraft {
}

repositories {
  maven(url = "https://maven.shadowfacts.net")
  maven(url = "https://grondag-repo.appspot.com") {
    credentials { username = "guest"; password = "" }
  }
}

dependencies {
  minecraft("com.mojang:minecraft:1.14")
  mappings("net.fabricmc:yarn:1.14+build.1")
  modCompile("net.fabricmc:fabric-loader:0.4.2+build.132")

  // Fabric API. This is technically optional, but you probably want it anyway.
  modCompile("net.fabricmc:fabric:0.2.7+build.127")
  compile("net.fabricmc:fabric-language-kotlin:1.3.30+build.2")
  compileOnly(kotlin("stdlib", "1.3.30"))
  compileOnly(kotlin("stdlib-jdk8", "1.3.30"))

  // modCompile("grondag", "frex", "0.1.112-alpha")
  // modCompile("grondag", "indigo", "0.1.386-alpha")
}
