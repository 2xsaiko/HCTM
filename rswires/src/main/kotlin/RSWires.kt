package therealfarfetchd.rswires

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.util.Identifier
import therealfarfetchd.hctm.common.util.ext.makeStack
import therealfarfetchd.rswires.common.init.BlockEntityTypes
import therealfarfetchd.rswires.common.init.Blocks
import therealfarfetchd.rswires.common.init.Items

const val ModID = "rswires"

object RSWires : ModInitializer {
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
  }
}