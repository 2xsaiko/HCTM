package therealfarfetchd.retrocomputers.common.block.capability

import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.retrocomputers.common.api.block.BusContainer
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer
import therealfarfetchd.retrocomputers.common.api.component.ComponentBusAware
import therealfarfetchd.retrocomputers.common.block.Rack

class RackBusDataContainer(val rack: Rack.Tile, ns: INeighborSupport<BusContainer>) : BusDataContainer(ns) {
  override fun peek(busId: Byte, addr: Byte): Byte? {
    return rack.container.getComponents()
      .mapNotNull { it as? ComponentBusAware }
      .firstOrNull { it.busId == busId }
      ?.peek(addr)
  }

  override fun poke(busId: Byte, addr: Byte, v: Byte) {
    rack.container.getComponents()
      .mapNotNull { it as? ComponentBusAware }
      .firstOrNull { it.busId == busId }
      ?.poke(addr, v)
  }
}