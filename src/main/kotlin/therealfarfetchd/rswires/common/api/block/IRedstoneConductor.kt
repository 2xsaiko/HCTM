package therealfarfetchd.rswires.common.api.block

import therealfarfetchd.quacklib.common.api.INeighborSupport

interface IRedstoneConductor {
  val ns: INeighborSupport<IRedstoneConductor>

  val wireType: RedstoneWireType

  fun isPropagating(channel: Any?): Boolean

  fun setPropagating(channel: Any?, b: Boolean)

  fun allowChannel(otherType: RedstoneWireType, otherChannel: Any?): Boolean

  fun mapChannel(otherType: RedstoneWireType, otherChannel: Any?): Any?

  fun getInput(channel: Any?): Boolean

  fun setOutput(channel: Any?, b: Boolean)

  fun endPropagating(channel: Any?)
}