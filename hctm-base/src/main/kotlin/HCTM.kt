package net.dblsaiko.hctm

import net.dblsaiko.hctm.common.init.Packets
import net.dblsaiko.hctm.common.wire.getWireNetworkState
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback

const val ModID = "hctm-base"

object HCTM : ModInitializer {

  override fun onInitialize() {
    Packets

    ServerTickCallback.EVENT.register(ServerTickCallback {
      it.worlds.forEach { it.getWireNetworkState().controller.flushUpdates() }
    })
  }

}