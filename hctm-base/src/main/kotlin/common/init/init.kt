package therealfarfetchd.hctm.common.init

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.util.Identifier
import therealfarfetchd.hctm.ModID
import therealfarfetchd.hctm.client.packet.onDebugNetUpdateResponse
import therealfarfetchd.hctm.common.packet.onDebugNetUpdateRequest

object Packets {

  object Client {
    val DebugNetResponse = Identifier(ModID, "debug_net_recv")
  }

  object Server {
    val DebugNetRequest = Identifier(ModID, "debug_net_req")
  }

  init {
    ClientSidePacketRegistry.INSTANCE.register(Client.DebugNetResponse, ::onDebugNetUpdateResponse)
    ServerSidePacketRegistry.INSTANCE.register(Server.DebugNetRequest, ::onDebugNetUpdateRequest)
  }

}