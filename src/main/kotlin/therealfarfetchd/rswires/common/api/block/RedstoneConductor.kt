package therealfarfetchd.rswires.common.api.block

import therealfarfetchd.quacklib.common.api.INeighborSupport

open class RedstoneConductor<T>(
  override val ns: INeighborSupport<IRedstoneConductor>,
  private val rsIn: (T) -> Boolean,
  private val rsOut: (T, Boolean) -> Unit,
  private val endCallback: (T) -> Unit,
  private val wireTypeIn: () -> RedstoneWireType,
  private val channelMapper: (other: RedstoneWireType, otherChannel: Any?) -> T,
  private val channelFilter: (other: RedstoneWireType, otherChannel: Any?) -> Boolean
) : IRedstoneConductor {
  private var propagatingSet: Set<Any?> = emptySet()

  override fun isPropagating(channel: Any?): Boolean = channel in propagatingSet

  override fun setPropagating(channel: Any?, b: Boolean) {
    if (b) propagatingSet += channel
    else propagatingSet -= channel
  }

  override fun allowChannel(otherType: RedstoneWireType, otherChannel: Any?): Boolean = channelFilter(otherType, otherChannel)

  override fun mapChannel(otherType: RedstoneWireType, otherChannel: Any?): Any? = channelMapper(otherType, otherChannel)

  override val wireType: RedstoneWireType
    get() = wireTypeIn()

  @Suppress("UNCHECKED_CAST")
  override fun getInput(channel: Any?): Boolean {
    return rsIn(channel as T)
  }

  @Suppress("UNCHECKED_CAST")
  override fun setOutput(channel: Any?, b: Boolean) {
    rsOut(channel as T, b)
  }

  @Suppress("UNCHECKED_CAST")
  override fun endPropagating(channel: Any?) {
    endCallback(channel as T)
  }
}