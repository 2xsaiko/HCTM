package net.dblsaiko.rswires.client

import net.dblsaiko.hctm.client.render.model.UnbakedWireModel
import net.dblsaiko.rswires.ModID
import net.dblsaiko.rswires.common.init.Blocks
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object RSWiresClient : ClientModInitializer {

  override fun onInitializeClient() {
    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
      val redAlloyOffModel = UnbakedWireModel(Identifier(ModID, "block/red_alloy_wire/off"), 0.125f, 0.125f, 32.0f)
      val redAlloyOnModel = UnbakedWireModel(Identifier(ModID, "block/red_alloy_wire/on"), 0.125f, 0.125f, 32.0f)

      val insulatedWireOffModel = DyeColor.values().associate { it to UnbakedWireModel(Identifier(ModID, "block/insulated_wire/${it.getName()}/off"), 0.25f, 0.1875f, 32.0f) }
      val insulatedWireOnModel = DyeColor.values().associate { it to UnbakedWireModel(Identifier(ModID, "block/insulated_wire/${it.getName()}/on"), 0.25f, 0.1875f, 32.0f) }

      val colorBundledCableModel = DyeColor.values().associate { it to UnbakedWireModel(Identifier(ModID, "block/bundled_cable/${it.getName()}"), 0.375f, 0.25f, 32.0f) }
      val plainBundledCableModel = UnbakedWireModel(Identifier(ModID, "block/bundled_cable/none"), 0.375f, 0.25f, 32.0f)

      ModelVariantProvider { modelId, _ ->
        val props = modelId.variant.split(",")
        when (val id = Identifier(modelId.namespace, modelId.path)) {
          Registry.BLOCK.getId(Blocks.RedAlloyWire) -> {
            if ("powered=false" in props) redAlloyOffModel
            else redAlloyOnModel
          }
          in Blocks.InsulatedWires.values.asSequence().map(Registry.BLOCK::getId) -> {
            val (color, _) = Blocks.InsulatedWires.entries.first { (_, block) -> id == Registry.BLOCK.getId(block) }
            if ("powered=false" in props) insulatedWireOffModel.getValue(color)
            else insulatedWireOnModel.getValue(color)
          }
          Registry.BLOCK.getId(Blocks.UncoloredBundledCable) -> {
            plainBundledCableModel
          }
          in Blocks.ColoredBundledCables.values.asSequence().map(Registry.BLOCK::getId) -> {
            val (color, _) = Blocks.ColoredBundledCables.entries.first { (_, block) -> id == Registry.BLOCK.getId(block) }
            colorBundledCableModel.getValue(color)
          }
          else -> null
        }
      }
    }
  }

}