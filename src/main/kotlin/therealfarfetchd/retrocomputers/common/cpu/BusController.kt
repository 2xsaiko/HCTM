package therealfarfetchd.retrocomputers.common.cpu

import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer

interface BusController {
  var targetBus: Byte
  val isBusConnected: Boolean
  val busFailed: Boolean
  fun bus(): BusDataContainer?

  fun resetBusState()
  var allowWrite: Boolean

  var writePos: Short
}