package net.dblsaiko.hctm.client.render.model

import net.minecraft.block.BlockState
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.resource.ResourceManager

object ModelWrapperHandler {

  private val handlers = mutableListOf<(ResourceManager) -> (BlockState, UnbakedModel) -> UnbakedModel>()

  fun register(op: (ResourceManager) -> (BlockState, UnbakedModel) -> UnbakedModel) {
    handlers += op
  }

  fun prepare(resourceManager: ResourceManager): (BlockState, UnbakedModel) -> UnbakedModel {
    val list = handlers.map { it(resourceManager) }
    return { id, model -> list.fold(model) { acc, a -> a(id, acc) } }
  }

}