package net.dblsaiko.hctm.common.init

import net.dblsaiko.hctm.ModID
import net.dblsaiko.hctm.client.packet.onDebugNetUpdateResponse
import net.dblsaiko.hctm.common.packet.onDebugNetUpdateRequest
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.util.Identifier

object Packets {

  object Client {

    val DebugNetResponse = Identifier(ModID, "debug_net_recv")

    fun register() {
      ClientSidePacketRegistry.INSTANCE.register(Client.DebugNetResponse, ::onDebugNetUpdateResponse)
    }

  }

  object Server {

    val DebugNetRequest = Identifier(ModID, "debug_net_req")

    fun register() {
      ServerSidePacketRegistry.INSTANCE.register(Server.DebugNetRequest, ::onDebugNetUpdateRequest)
    }

  }

}