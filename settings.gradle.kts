pluginManagement {
  repositories {
    jcenter()
      maven(url = "http://maven.fabricmc.net/") {
      name = "Fabric"
    }
    gradlePluginPortal()
  }
}

rootProject.name = "HCTM"

include("hctm-base")
include("retrocomputers")
include("rswires")