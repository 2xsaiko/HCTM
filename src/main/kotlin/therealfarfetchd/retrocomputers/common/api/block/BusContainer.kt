package therealfarfetchd.retrocomputers.common.api.block

import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.quacklib.common.api.util.EnumFaceLocation
import therealfarfetchd.quacklib.common.api.util.EnumFacingExtended
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

open class BusContainer(val ns: INeighborSupport<BusContainer>) {
  /**
   * @return all objects connected together
   */
  fun resolveNetwork(): Set<BusContainer> {
    var set = emptySet<BusContainer>()
    var newest = setOf(this)
    while (newest.isNotEmpty()) {
      val newer = newest.flatMap { EnumFacingExtended.Values.map(it.ns::getNeighborAt) }.filterNotNull() - set
      set += newer
      newest = newer.toSet()
    }
    return set
  }

  open fun save(nbt: QNBTCompound) {}
  open fun load(nbt: QNBTCompound) {}
}

open class BusDataContainer(val peek: (Byte) -> Byte, val poke: (Byte, Byte) -> Unit, ns: INeighborSupport<BusContainer>) : BusContainer(ns) {
  var busId: Byte = 0

  override fun save(nbt: QNBTCompound) {
    super.save(nbt)
    nbt.byte["B"] = busId
  }

  override fun load(nbt: QNBTCompound) {
    super.load(nbt)
    busId = nbt.byte["B"]
  }
}