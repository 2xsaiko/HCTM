package net.dblsaiko.hctm

import net.dblsaiko.hctm.common.init.Packets
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback

object HCTMClient : ClientModInitializer {

  override fun onInitializeClient() {
    Packets.Client.register()
  }

}