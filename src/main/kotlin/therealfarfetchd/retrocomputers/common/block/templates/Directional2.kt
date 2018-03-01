package therealfarfetchd.retrocomputers.common.block.templates

import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.qblock.QBlock
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.common.block.templates.Directional.Companion.PropFacing

abstract class Directional2 : QBlock() {
  var facing: EnumFacing = EnumFacing.DOWN; protected set

  override val properties: Set<IProperty<*>> = super.properties + PropFacing

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    facing = sidePlaced
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    if (world.isServer) {
      facing = facing.rotateAround(axis.axis)
      world.notifyNeighborsOfStateChange(pos, container.blockType, true)
      dataChanged()
      clientDataChanged()
    }
    return true
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["F"] = facing.index
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getFront(nbt.ubyte["F"])
  }

  override fun applyProperties(state: IBlockState): IBlockState = super.applyProperties(state).withProperty(PropFacing, facing)
}