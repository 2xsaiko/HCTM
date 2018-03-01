package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef

@BlockDef(registerModels = false, creativeTab = ModID)
class Fabricator : BlockPowered() {

  override fun getItem(): ItemStack = Item.makeStack()

  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override val material: Material = Material.ROCK
  override val blockType: ResourceLocation = ResourceLocation(ModID, "fabricator")
  override val properties: Set<IProperty<*>> = super.properties + PropActive

  companion object {
    val PropActive = PropertyBool.create("active")

    val Block by WrapperImplManager.container(Fabricator::class)
    val Item by WrapperImplManager.item(Fabricator::class)
  }
}