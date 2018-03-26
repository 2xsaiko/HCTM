package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.FillBlocksScope
import therealfarfetchd.quacklib.common.api.qblock.IQBlockMultiblock
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef

@BlockDef(registerModels = false)
class KineticGenerator : BlockPowered(), IQBlockMultiblock {
  override fun FillBlocksScope.fillBlocks() {
  }

  override val material = Material.IRON
  override val blockType = ResourceLocation(ModID, "kinetic_generator")

  override fun getItem() = Item.makeStack()

  companion object {
    val Block by WrapperImplManager.container(KineticGenerator::class)
    val Item by WrapperImplManager.item(KineticGenerator::class)
  }
}