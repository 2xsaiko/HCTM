package therealfarfetchd.powerline.common.api

import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.EnumFacingExtended
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import java.lang.Math.abs
import kotlin.collections.component1
import kotlin.collections.component2

class PowerConductor(val ns: INeighborSupport<PowerConductor>, val conf: IConductorConfiguration) {

  var voltage: Double = 0.0 // The voltage, in V
  var current: Double = 0.0 // The current, in A
  val power get() = voltage * current // The power, in W

  private var chargeBuf: Int = 0
  val chargeLevel: Int get() = java.lang.Integer.bitCount(chargeBuf) * 100 / 32

  private var _currentTr: Map<EnumFacingExtended, Double> = emptyMap()

  private var currentTr: Map<PowerConductor, Double> = emptyMap()
    get() {
      if (_currentTr.isNotEmpty()) {
        computeConductorMap()
        field += _currentTr.toList().mapFirstNotNull(conductorCache::get)
        _currentTr = emptyMap()
      }
      return field
    }
  private var transferCache: Set<PowerConductor> = emptySet()

  private var conductorCache: Map<EnumFacingExtended, PowerConductor> = emptyMap()
  private var conductorCacheR: Map<PowerConductor, EnumFacingExtended> = emptyMap()

  val cl = ChangeListener(this::voltage, this::current, this::chargeBuf, this::currentTr)

  fun applyPower(p: Double) {
    voltage += p / 20
    current += abs(p / 20)
  }

  private fun computeConductorMap() {
    if (conductorCache.isNotEmpty()) return
    conductorCache = EnumFacingExtended.Values
      .mapWithCopy(ns::getNeighborAt)
      .filterSecondNotNull()
      .toMap()
    conductorCacheR = conductorCache.asReversed()
  }

  fun update() {
    current = 0.0

    if (voltage > conf.voltageSpec) {
      val vdiff = voltage - conf.voltageSpec
      voltage -= vdiff * 0.1
    }

    conductorCache = emptyMap()
    conductorCacheR = emptyMap()
    computeConductorMap()

    conductorCacheR.toList()
      .filterNot { transferCurrent(it.second, it.first) }
      .forEach { currentTr -= it.first }

    if (conf.powerThreshold > 0) {
      chargeBuf = (chargeBuf shl 1) + if (voltage >= conf.powerThreshold) 1 else 0
    }

    transferCache = emptySet()
  }

  private fun getAccelFactor(x: Double): Double {
    val y = (x / conf.voltageSpec)
    val z = -(y * y * y)
    return maxOf(0.0, minOf(z + 1, 1.0))
  }

  private fun transferCurrent(l: EnumFacingExtended, condIn: PowerConductor?): Boolean = ensureOnce(condIn) { cond ->
    val vdiff = voltage - cond.voltage
    var c = (currentTr[cond] ?: 0.0)
    val r = conf.resistance + cond.conf.resistance
    val t = c / r
    c *= conf.decel
    c += vdiff * getAccelFactor(vdiff) / 10000
    currentTr += cond to c
    cond.currentTr += this to -c

    voltage -= t / conf.capacitance
    cond.voltage += t / cond.conf.capacitance
    current += abs(t / 2)
    cond.current += abs(t / 2)
  }

  private fun ensureOnce(cond: PowerConductor?, op: (cond: PowerConductor) -> Unit): Boolean {
    cond ?: return false

    val b = this !in cond.transferCache
    cond.transferCache += this
    if (b) {
      op(cond)
    }
    return b
  }

  fun save(nbt: QNBTCompound) {
    nbt.double["v"] = voltage
    nbt.double["i"] = current
    if (chargeBuf != 0)
      nbt.int["p"] = chargeBuf

    nbt.nbt["t"].also {
      for ((c, d) in currentTr) {
        val efl = conductorCacheR[c]
        if (efl != null) it.double[efl.ordinal.toString(16)] = d
      }
    }
  }

  fun load(nbt: QNBTCompound) {
    voltage = nbt.double["v"]
    current = nbt.double["i"]
    chargeBuf = nbt.int["p"]

    currentTr = emptyMap()
    nbt.nbt["t"].keys
      .mapWithCopy { EnumFacingExtended.Values[it.toInt(16)] }
      .mapFirst(nbt.double::get)
      .forEach { _currentTr += it.swap() }
  }
}