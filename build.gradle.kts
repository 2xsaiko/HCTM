@file:Suppress("PropertyName", "LocalVariableName")

import net.minecraftforge.gradle.user.UserBaseExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalField

val mod_name: String by extra
val mod_version: String by extra
val mc_version: String by extra
val forge_version: String by extra
val mappings_version: String by extra
val kotlin_version: String by extra
val forgelin_version: String by extra
val quacklib_version: String by extra
val mcmp_version: String by extra
val jei_version: String by extra

val hctm_build: String = DateTimeFormatter.ofPattern("YYYYMMddHHmmss").format(LocalDateTime.now())

val Project.minecraft: UserBaseExtension
  get() = extensions.getByName<UserBaseExtension>("minecraft")

buildscript {
  val kotlin_version: String by extra
  repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("http://files.minecraftforge.net/maven") }
  }
  dependencies {
    classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    classpath(kotlin("gradle-plugin", kotlin_version))
  }
}

plugins { java }

apply {
  plugin("net.minecraftforge.gradle.forge")
  plugin("kotlin")
}

version = mod_version
group = "therealfarfetchd"

repositories {
  maven { setUrl("https://modmaven.k-4u.nl/") }
  maven { setUrl("http://maven.shadowfacts.net/") }
}

dependencies {
  deobfCompile("quacklib", "quacklib", quacklib_version)
  deobfCompile("MCMultiPart2", "MCMultiPart-exp", mcmp_version)
  compile("net.shadowfacts", "Forgelin", forgelin_version)

  runtime("mezz.jei", "jei_$mc_version", jei_version)
  deobfProvided("mezz.jei", "jei_$mc_version", jei_version, classifier = "api")
}

configure<UserBaseExtension> {
  version = "$mc_version-$forge_version"
  runDir = "run"
  mappings = mappings_version
}

tasks.withType<JavaCompile> {
  sourceCompatibility = "1.8"
  targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
  sourceCompatibility = "1.8"
  targetCompatibility = "1.8"
}

tasks.withType<Jar> {
  doFirst {
    println("Building HCTM version $hctm_build")
  }

  inputs.properties += "version" to project.version
  inputs.properties += "mcversion" to project.minecraft.version

  baseName = mod_name

  filesMatching("/mcmod.info") {
    expand(mapOf(
      "version" to project.version,
      "mcversion" to project.minecraft.version,
      "quacklib_version" to quacklib_version,
      "forgelin_version" to forgelin_version,
      "hctm_build" to hctm_build,
      "mcmp_version" to mcmp_version
    ))
  }
}

fun DependencyHandler.deobfCompile(
  group: String,
  name: String,
  version: String? = null,
  configuration: String? = null,
  classifier: String? = null,
  ext: String? = null): ExternalModuleDependency =
  create(group, name, version, configuration, classifier, ext).apply { add("deobfCompile", this) }

fun DependencyHandler.deobfProvided(
  group: String,
  name: String,
  version: String? = null,
  configuration: String? = null,
  classifier: String? = null,
  ext: String? = null): ExternalModuleDependency =
  create(group, name, version, configuration, classifier, ext).apply { add("deobfProvided", this) }