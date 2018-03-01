package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.DefaultConductorConfiguration
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.QBlockConnectable
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef

@BlockDef
class Transformer : QBlockConnectable() {
  var facing: EnumFacing = EnumFacing.NORTH

  val lvcond = PowerConductor(neighborSupport { it.base == facing && it.side == EnumFacing.DOWN }, DefaultConductorConfiguration.LVCable)
  val hvcond = PowerConductor(neighborSupport { it.base == facing.opposite && it.side == EnumFacing.DOWN }, DefaultConductorConfiguration.HVCable)

  override fun getItem(): ItemStack = Item.makeStack()

  override val material: Material = Material.IRON
  override val blockType: ResourceLocation = ResourceLocation(ModID, "transformer")

  companion object {
    val Block by WrapperImplManager.container(Transformer::class)
    val Item by WrapperImplManager.item(Transformer::class)
  }
}