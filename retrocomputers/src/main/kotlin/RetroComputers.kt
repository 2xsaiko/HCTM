package net.dblsaiko.retrocomputers

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.retrocomputers.common.ClientProxy
import net.dblsaiko.retrocomputers.common.Proxy
import net.dblsaiko.retrocomputers.common.ServerProxy
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.dblsaiko.retrocomputers.common.init.Blocks
import net.dblsaiko.retrocomputers.common.init.Items
import net.dblsaiko.retrocomputers.common.init.Packets
import net.dblsaiko.retrocomputers.common.init.Resources
import net.fabricmc.api.EnvType.CLIENT
import net.fabricmc.api.EnvType.SERVER
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier

const val MOD_ID = "retrocomputers"

object RetroComputers : ModInitializer {

  lateinit var proxy: Proxy

  override fun onInitialize() {
    proxy = when (FabricLoader.getInstance().environmentType!!) {
      CLIENT -> ClientProxy()
      SERVER -> ServerProxy()
    }

    Blocks
    BlockEntityTypes
    Items
    Packets
    Resources

    FabricItemGroupBuilder.create(Identifier(MOD_ID, "all"))
      .icon { Items.COMPUTER.makeStack() }
      .appendItems {
        it += Items.COMPUTER.makeStack()
        it += Items.TERMINAL.makeStack()
        it += Items.DISK_DRIVE.makeStack()
        it += Items.RIBBON_CABLE.makeStack()
        it += Items.USER_DISK.makeStack()
        it += Items.SYS_DISKS.map { it.makeStack() }
      }.build()
  }

}