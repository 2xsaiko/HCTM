package net.dblsaiko.hctm.client

import net.dblsaiko.hctm.common.init.Packets
import net.fabricmc.api.ClientModInitializer

object HCTMClient : ClientModInitializer {

  override fun onInitializeClient() {
    Packets.Client.register()
  }

}