package therealfarfetchd.powerline.common.api

interface IPowerConductor {

  val voltage: Double
  val current: Double
  val power: Double get() = voltage * current

  fun applyPower(p: Double)

  fun update()

}