package therealfarfetchd.rswires.common.block

import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.EnumFaceLocation
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.wires.BlockWire
import therealfarfetchd.rswires.common.api.block.RedstoneConductor
import therealfarfetchd.rswires.common.api.block.RedstoneWireType
import therealfarfetchd.rswires.common.api.block.capability.IRedstoneConnectable
import therealfarfetchd.rswires.common.util.NetworkPropagator

abstract class RSBaseWire<T>(width: Double, height: Double) : BlockWire<RedstoneConductor<T>>(width, height) {
  protected var active: Set<T> = emptySet()
  protected var lastActive: Set<T> = emptySet()
  protected var rsUpdate: Set<T> = emptySet()

  override fun onAdded() {
    super.onAdded()
    for (ch in getValidChannels())
      NetworkPropagator.schedulePropagate(data, ch)
  }

  override fun onNeighborChanged(side: EnumFacing) {
    super.onNeighborChanged(side)
    for (ch in getValidChannels())
      NetworkPropagator.schedulePropagate(data, ch)
  }

  override fun onBreakBlock() {
    for (ch in getValidChannels()) {
      EnumFaceLocation.Values
        .mapNotNull(data.ns::getNeighborAt)
        .filter { it.allowChannel(data.wireType, ch) }
        .mapWithCopy { it.mapChannel(data.wireType, ch) }
        .toSet()
        .forEach { NetworkPropagator.schedulePropagate(it.first, it.second) }
    }
    super.onBreakBlock()
  }

  protected fun notifyWireNeighborsOfStateChange() {
    world.notifyNeighborsOfStateChange(pos, container.blockType, false)

    for (enumfacing in EnumFacing.values()) {
      world.notifyNeighborsOfStateChange(pos.offset(enumfacing), container.blockType, false)
    }
  }

  abstract fun getWireType(): RedstoneWireType

  private fun onPropagationEnd(channel: T) {
    if (channel in active != channel in lastActive) {
      try {
        rsUpdate += channel
        onPropagated(channel)
      } finally {
        rsUpdate -= channel
        if (channel in active) lastActive += channel
        else lastActive -= channel
      }
    }
  }

  protected open fun onPropagated(channel: T) {
    dataChanged()
  }

  abstract fun getInput(channel: T): Boolean

  private fun setOutput(channel: T, b: Boolean) {
    if (!b) active -= channel
    else active += channel
  }

  abstract fun mapChannel(otherType: RedstoneWireType, otherChannel: Any?): T

  abstract fun filterChannel(otherType: RedstoneWireType, otherChannel: Any?): Boolean

  abstract fun saveChannelData(nbt: QNBTCompound)
  abstract fun loadChannelData(nbt: QNBTCompound)

  abstract fun getValidChannels(): Set<T>

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) saveChannelData(nbt)
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) loadChannelData(nbt)
    lastActive = active
  }

  @Suppress("LeakingThis")
  override val data: RedstoneConductor<T> = RedstoneConductor(neighborSupport(), this::getInput, this::setOutput,
    this::onPropagationEnd, this::getWireType, this::mapChannel, this::filterChannel)
  override val dataType: ResourceLocation = IRedstoneConnectable.DataType
}