package net.dblsaiko.rswires

import net.dblsaiko.rswires.common.block.RedstoneWireUtils
import net.dblsaiko.rswires.common.init.BlockEntityTypes
import net.dblsaiko.rswires.common.init.Blocks
import net.dblsaiko.rswires.common.init.Items
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.server.world.ServerWorld

const val MOD_ID = "rswires"

object RSWires : ModInitializer {
  var wiresGivePower = true

  override fun onInitialize() {
    BlockEntityTypes.register()
    Blocks.register()
    Items.register()

    WorldTickCallback.EVENT.register(WorldTickCallback {
      if (it is ServerWorld) {
        RedstoneWireUtils.flushUpdates(it)
      }
    })
  }
}