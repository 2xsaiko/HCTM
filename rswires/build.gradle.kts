plugins {
  kotlin("jvm")
}

base {
  archivesBaseName = "rswires"
}

dependencies {
  compile(project(":hctm-base"))
}