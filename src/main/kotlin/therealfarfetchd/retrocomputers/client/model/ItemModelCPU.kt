package therealfarfetchd.retrocomputers.client.model

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemOverrideList
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import therealfarfetchd.quacklib.client.api.model.CachedBakedModel
import therealfarfetchd.quacklib.client.api.model.registerBakedModel
import therealfarfetchd.quacklib.common.api.util.AutoLoad
import therealfarfetchd.retrocomputers.client.render.component.RenderCPU
import therealfarfetchd.retrocomputers.common.item.ItemCPU

@SideOnly(Side.CLIENT)
@AutoLoad
object ItemModelCPU {
  object Renderer : TileEntityItemStackRenderer() {
    override fun renderByItem(stack: ItemStack, partialTicks: Float) {
      RenderCPU.obj.reset()
      RenderCPU.obj.draw()
    }
  }

  object BakedModel : IBakedModel {
    override fun getParticleTexture() = null

    override fun getQuads(state: IBlockState?, side: EnumFacing?, rand: Long): List<BakedQuad> = emptyList()

    override fun getItemCameraTransforms() = CachedBakedModel.blockItemCameraTransforms

    override fun isBuiltInRenderer() = true

    override fun isAmbientOcclusion() = true

    override fun isGui3d() = false

    override fun getOverrides() = ItemOverrideList.NONE
  }

  object Tile : TileEntity()

  init {
    ItemCPU.tileEntityItemStackRenderer = Renderer
    BakedModel.registerBakedModel(ModelResourceLocation("retrocomputers:cpu2", "inventory"))
  }
}