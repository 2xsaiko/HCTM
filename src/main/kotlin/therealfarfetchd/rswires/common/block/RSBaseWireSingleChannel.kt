package therealfarfetchd.rswires.common.block

import mcmultipart.api.container.IPartInfo
import mcmultipart.api.slot.EnumFaceSlot
import mcmultipart.api.slot.SlotUtil
import mcmultipart.block.BlockMultipartContainer
import mcmultipart.block.TileMultipartContainer
import mcmultipart.multipart.PartInfo
import net.minecraft.block.BlockRedstoneWire
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import therealfarfetchd.quacklib.common.api.extensions.getQBlock
import therealfarfetchd.quacklib.common.api.extensions.getStrongPower
import therealfarfetchd.quacklib.common.api.qblock.IQBlockRedstone
import therealfarfetchd.quacklib.common.api.qblock.QBContainerTile
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.EnumFaceLocation
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.wires.BlockWire
import therealfarfetchd.rswires.common.api.block.RedstoneWireType
import therealfarfetchd.rswires.common.block.RSBaseWireSingleChannel.UKey
import java.util.function.Function

abstract class RSBaseWireSingleChannel(width: Double, height: Double) : RSBaseWire<UKey>(width, height), IQBlockRedstone {
  // for adapting to insulated wire
  abstract val providePowerToGround: Boolean

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state).withProperty(PropPowered, UKey in active)
  }

  override fun connectsToOther(thisBlock: BlockPos, e: EnumFaceLocation): Boolean {
    // TODO outbound collision check
    val f = e.base
    val connectionPos = pos.offset(f)
    val state = world.getBlockState(connectionPos)
    if (state.block.canConnectRedstone(state, world, connectionPos, f)) {
      // we won't connect to wires because of redstone output
      val isWire = (e.side?.let { world.getQBlock(connectionPos, EnumFaceSlot.fromFace(it)) } ?: world.getQBlock(connectionPos)) is BlockWire<*>
      if (!isWire) {
        val te = world.getTileEntity(connectionPos)
        if (te is BlockMultipartContainer) {
          // we don't want to include wires in the connections so we use a custom canConnectRedstone here
          val rsCap = BlockMultipartContainer.getTile(world, connectionPos)
            .map({ t ->
              SlotUtil.viewContainer(t, Function { i: IPartInfo ->
                if ((i.tile.tileEntity as? QBContainerTile)?.qb is BlockWire<*>) false
                else i.part.canConnectRedstone((i as PartInfo).wrapAsNeeded(world), pos, i, f.opposite)
              }, Function { l: MutableList<Boolean> -> l.stream().anyMatch { it } }, false, true, f.opposite)
            }).orElse(false)
          if (rsCap) return true
        }
        return true
      }
    }
    return false
  }

  override fun getOutput(side: EnumFacing, strong: Boolean) =
    if (data.isPropagating(UKey) && UKey !in rsUpdate) 0
    else when (side) {
      in validSides[facing]!! -> if (!strong) getSignalLevel() else 0
      facing.opposite -> if (providePowerToGround || !strong) getSignalLevel() else 0
      else -> 0
    }

  private fun getInputStrength() =
    (validSides[facing]!!
       .filter {
         world.getBlockState(pos.offset(it)).block !is BlockRedstoneWire &&
         world.getQBlock(pos.offset(it)) !is BlockWire<*>
       }
       .map {
         val p = pos.offset(it)
         val state = world.getBlockState(p)
         val te = world.getTileEntity(p)
         when (te) {
           is TileMultipartContainer ->
             te.getStrongPower(it, { (it.tile as? QBContainerTile)?.qb !is BlockWire<*> })
           else ->
             if (state.block.shouldCheckWeakPower(state, world, p, it)) state.getStrongPower(world, p, it)
             else state.getWeakPower(world, p, it)
         }
       }.max() ?: 0
    ).let { out ->
      if (!providePowerToGround) out
      else maxOf(out, world.getRedstonePower(pos.offset(facing), facing))
    }

  override fun getInput(channel: UKey): Boolean = getInputStrength() > 0

  override fun onPropagated(channel: UKey) {
    super.onPropagated(channel)
    clientDataChanged()
    notifyWireNeighborsOfStateChange()
  }

  override fun saveChannelData(nbt: QNBTCompound) {
    nbt.bool["s"] = UKey in active
  }

  override fun loadChannelData(nbt: QNBTCompound) {
    active = emptySet()
    if (nbt.bool["s"]) active += UKey
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Client) saveChannelData(nbt)
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Client) loadChannelData(nbt)
  }

  private fun getSignalLevel() = if (UKey in active) 15 else 0
  override fun getValidChannels() = setOf(UKey)
  override fun mapChannel(otherType: RedstoneWireType, otherChannel: Any?): UKey = UKey
  override fun canConnect(side: EnumFacing): Boolean = side in validSides[facing]!!

  override val properties: Set<IProperty<*>> = super.properties + PropPowered

  companion object {
    val PropPowered: PropertyBool = PropertyBool.create("powered")
  }

  object UKey
}