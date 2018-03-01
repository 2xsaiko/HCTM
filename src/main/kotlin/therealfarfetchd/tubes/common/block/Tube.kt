package therealfarfetchd.tubes.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.client.api.model.wire.CenteredWireModel
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.clientOnly
import therealfarfetchd.quacklib.common.api.wires.BlockWireCentered
import therealfarfetchd.tubes.ModID
import therealfarfetchd.tubes.common.api.block.capability.ITubeInterface
import therealfarfetchd.tubes.common.api.item.ColoredItemStack

@BlockDef(registerModels = false, creativeTab = ModID)
open class Tube : BlockWireCentered<ITubeInterface>(0.5) {
  override fun getItem(): ItemStack = Item.makeStack()

  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT
  override val blockType: ResourceLocation = ResourceLocation(ModID, "tube")
  override val dataType: ResourceLocation = ITubeInterface.DataType
  override val data: ITubeInterface = object : ITubeInterface {
    override fun canAcceptItem(c: ColoredItemStack): Boolean {
      TODO("not implemented")
    }

    override fun acceptItem(c: ColoredItemStack): ColoredItemStack {
      TODO("not implemented")
    }
  }

  //  private val box = AdvancedBoundingBox.fromMatrix(Mat4.Identity
  //    .translate(0.5f, 0.5f, 0.5f)
  //    .scale(1f, 1f, 1f)
  //    .rotate(1f, 1f, 0f, 45f)
  //    .translate(-0.5f, -0.5f, -0.5f))

  //  override val selectionBox: Collection<AxisAlignedBB>
  //    get() = super.collisionBox

  //  override val collisionBox
  //    get() = box.getBoxes()

  companion object {
    val Item by WrapperImplManager.item(Tube::class)
    val Block by WrapperImplManager.container(Tube::class)

    val Bakery by clientOnly { CenteredWireModel(ResourceLocation(ModID, "blocks/tube"), 16.0f, 0.5) }
  }
}