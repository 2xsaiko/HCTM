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
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal.Companion.PropFacing

abstract class Horizontal2 : QBlock() {
  var facing: EnumFacing = EnumFacing.NORTH; protected set

  override val properties: Set<IProperty<*>> = super.properties + PropFacing

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    placer?.also { facing = it.adjustedHorizontalFacing.opposite }
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    if (world.isServer) {
      facing = facing.rotateY()
      world.notifyNeighborsOfStateChange(pos, container.blockType, true)
      dataChanged()
      clientDataChanged()
    }
    return true
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["F"] = facing.horizontalIndex
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getHorizontal(nbt.ubyte["F"])
  }

  override fun applyProperties(state: IBlockState): IBlockState = super.applyProperties(state).withProperty(PropFacing, facing)
}