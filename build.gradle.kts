@file:Suppress("PropertyName", "LocalVariableName", "UNCHECKED_CAST", "UNNECESSARY_SAFE_CALL")

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.UUID

// import java.time.*
// import java.time.format.DateTimeFormatter

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

val min_quacklib_version: String by extra
val min_forgelin_version: String by extra
val min_mcmp_version: String by extra

// val hctm_build: String = DateTimeFormatter.ofPattern("YYYYMMddHHmmss").format(LocalDateTime.now())

var jarFile: FileTree by extra
var mcModInfo: Map<String, Any> by extra
mcModInfo = JsonSlurper().parse(File("src/main/resources/mcmod.info")) as Map<String, Any>

val Project.minecraft: ForgeExtension
  get() = extensions.getByName<ForgeExtension>("minecraft")

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

jarFile = zipTree((tasks.getByName("jar") as Jar).archivePath)

defComponentTask("retrocomputers")
defComponentTask("powerline")
defComponentTask("rswires")
defComponentTask("tubes")

repositories {
  maven { setUrl("https://modmaven.k-4u.nl/") }
  maven { setUrl("http://maven.shadowfacts.net/") }
}

dependencies {
  runtimeOnly("net.shadowfacts", "Forgelin", forgelin_version)
  compileOnly(kotlin("stdlib-jre8", kotlin_version))
  compileOnly(kotlin("reflect", kotlin_version))

  findProject(":QuackLib")?.also { compile(it) }
  ?: deobfCompile("quacklib", "quacklib", quacklib_version)

  deobfCompile("MCMultiPart2", "MCMultiPart-exp", mcmp_version)

  runtimeOnly("mezz.jei", "jei_$mc_version", jei_version)
  deobfProvided("mezz.jei", "jei_$mc_version", jei_version, classifier = "api")
}

configure<ForgeExtension> {
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
  // doFirst { println("Building HCTM version $hctm_build") }

  inputs.properties += "version" to project.version
  inputs.properties += "mcversion" to project.minecraft.version

  if (name == "jar")
    baseName = mod_name

  // from(java.sourceSets["main"].resources) {
  //   include("/mcmod.info")
  //   rename { "/assets/hctm-base/mod.info" }
  // }

  filesMatching("/mcmod.info") {
    expand(mapOf(
      "version" to project.version,
      "mcversion" to project.minecraft.version,
      "quacklib_version" to min_quacklib_version,
      "forgelin_version" to min_forgelin_version,
      "mcmp_version" to min_mcmp_version
    ))
  }
}

var counter = 0

fun createStrippedModInfo(modid: String): File {
  val f = File("build/processing/${counter++}/mcmod.info")
  f.parentFile.mkdirs()
  if (f.exists()) f.delete()
  f.createNewFile()
  val mod = (mcModInfo["modList"] as List<Map<String, Any>>).firstOrNull { it["modid"] == modid } ?: return f
  f.writeText(JsonOutput.toJson(mapOf(
    "modListVersion" to mcModInfo["modListVersion"],
    "modList" to listOf(mod)
  )))
  f.deleteOnExit()

  return f
}

fun defComponentTask(componentName: String) {
  val task = task<Jar>("${componentName}Jar") {
    dependsOn("reobfJar")

    from(jarFile) {
      // include("/mcmod.info")
      include("/pack.mcmeta")

      // include("/assets/hctm-base/**")
      // include("/therealfarfetchd/hctm/**")

      include("/assets/$componentName/**")
      include("/therealfarfetchd/$componentName/**")
    }

    from(createStrippedModInfo(componentName))

    version = mod_version
    baseName = componentName
  }
  artifacts { add("archives", task) }
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