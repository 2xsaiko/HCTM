package therealfarfetchd.rswires

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import therealfarfetchd.hctm.common.util.ext.makeStack
import therealfarfetchd.rswires.common.block.RedstoneWireUtils
import therealfarfetchd.rswires.common.init.BlockEntityTypes
import therealfarfetchd.rswires.common.init.Blocks
import therealfarfetchd.rswires.common.init.Items

const val ModID = "rswires"

object RSWires : ModInitializer {
  var wiresGivePower = true

  override fun onInitialize() {
    BlockEntityTypes
    Blocks
    Items

    FabricItemGroupBuilder.create(Identifier(ModID, "all"))
      .icon { Items.RedAlloyWire.makeStack() }
      .appendItems {
        it += Items.RedAlloyWire.makeStack()
        it += Items.InsulatedWires.values.map { it.makeStack() }
        it += Items.UncoloredBundledCable.makeStack()
        it += Items.ColoredBundledCables.values.map { it.makeStack() }
      }.build()

    WorldTickCallback.EVENT.register(WorldTickCallback {
      if (it is ServerWorld) {
        RedstoneWireUtils.flushUpdates(it)
      }
    })
  }
}