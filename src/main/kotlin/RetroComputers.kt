package therealfarfetchd.retrocomputers

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.util.Identifier
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes
import therealfarfetchd.retrocomputers.common.init.Blocks
import therealfarfetchd.retrocomputers.common.init.Items
import therealfarfetchd.retrocomputers.common.init.Packets
import therealfarfetchd.retrocomputers.common.util.ext.makeStack

const val ModID = "retrocomputers"

object RetroComputers : ModInitializer {

  override fun onInitialize() {
    BlockEntityTypes
    Blocks
    Items
    Packets

    FabricItemGroupBuilder.create(Identifier(ModID, "all"))
      .icon { Items.Computer.makeStack() }
      .appendItems {
        it += Items.Computer.makeStack()
        it += Items.Terminal.makeStack()
        it += Items.DiskDrive.makeStack()
      }
      .build()
  }

}