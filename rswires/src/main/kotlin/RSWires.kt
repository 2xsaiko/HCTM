package therealfarfetchd.rswires

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.server.world.ServerWorld
import therealfarfetchd.rswires.common.block.RedstoneWireUtils
import therealfarfetchd.rswires.common.init.BlockEntityTypes
import therealfarfetchd.rswires.common.init.Blocks
import therealfarfetchd.rswires.common.init.Items

const val ModID = "rswires"

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