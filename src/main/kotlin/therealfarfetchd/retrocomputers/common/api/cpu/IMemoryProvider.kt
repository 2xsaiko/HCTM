package therealfarfetchd.retrocomputers.common.api.cpu

import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer

/**
 * Created by marco on 24.06.17.
 */
interface IMemoryProvider {

  // read/write memory
  operator fun get(s: Short): Byte

  operator fun set(s: Short, b: Byte)

  val termAddr: Byte
  val diskAddr: Byte

  var targetBus: Byte
  val isBusConnected: Boolean
  val busFailed: Boolean
  fun bus(): BusDataContainer?

  fun resetBusState()
  var allowWrite: Boolean

  var writePos: Short
  fun halt()

}