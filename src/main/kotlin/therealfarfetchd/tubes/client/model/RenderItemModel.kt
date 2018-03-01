package therealfarfetchd.tubes.client.model

import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.init.Blocks
import therealfarfetchd.quacklib.client.api.model.IDynamicModel
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.QBlock
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.tubes.common.api.block.IFlowingItemProvider

class RenderItemModel<in T>: IDynamicModel<T> where T : QBlock, T : IFlowingItemProvider {
  override fun bakeDynamicQuads(block: T, playerPos: Vec3): List<Pair<BakedQuad, Boolean>> {
    val items = setOf(Blocks.STONEBRICK.makeStack())

    return emptyList() // TODO
  }
}