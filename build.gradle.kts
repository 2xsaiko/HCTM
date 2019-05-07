import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val minecraft_version: String by extra
val mappings_version: String by extra
val loader_version: String by extra
val mod_name: String by extra
val mod_version: String by extra
val fabric_api_version: String by extra
val fabric_kotlin_version: String by extra
val kotlin_version: String by extra
val frex_version: String by extra
val canvas_version: String by extra

plugins {
  kotlin("jvm") version "1.3.30"
  id("fabric-loom") version "0.2.2-SNAPSHOT"
}

allprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "fabric-loom")

  group = "therealfarfetchd.hctm"

  version = "1.0.0"

  java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
  }

  // task<Jar>("sourcesJar") {
  //   dependsOn("classes")
  //   classifier = "sources"
  //   from(sourceSets["main"].allSource)
  // }

  minecraft {
  }

  repositories {
    maven(url = "https://maven.shadowfacts.net")
    maven(url = "https://minecraft.curseforge.com/api/maven")
    maven(url = "https://grondag-repo.appspot.com") {
      credentials { username = "guest"; password = "" }
    }
  }

  dependencies {
    minecraft("com.mojang", "minecraft", minecraft_version)
    mappings("net.fabricmc", "yarn", mappings_version)
    modCompile("net.fabricmc", "fabric-loader", loader_version)

    // Fabric API. This is technically optional, but you probably want it anyway.
    modCompile("net.fabricmc", "fabric", fabric_api_version)
    compile("net.fabricmc", "fabric-language-kotlin", fabric_kotlin_version)
    compileOnly(kotlin("stdlib", kotlin_version))
    compileOnly(kotlin("stdlib-jdk8", kotlin_version))

    modCompile("grondag", "frex", frex_version)
    modCompile("grondag", "canvas", canvas_version)
    runtime("org.joml", "joml", "1.9.14") // fix your deps grondag
  }

}

dependencies {
  subprojects.forEach {
    compile(project(path = ":${it.name}"))
  }
}