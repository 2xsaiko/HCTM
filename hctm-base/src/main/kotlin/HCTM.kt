package therealfarfetchd.hctm

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import therealfarfetchd.hctm.common.init.Packets
import therealfarfetchd.hctm.common.wire.getWireNetworkState

const val ModID = "hctm-base"

object HCTM : ModInitializer {

  override fun onInitialize() {
    Packets

    ServerTickCallback.EVENT.register(ServerTickCallback {
      it.worlds.forEach { it.getWireNetworkState().controller.flushUpdates() }
    })
  }

}