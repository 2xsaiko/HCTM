package net.dblsaiko.hctm.common.init

import net.dblsaiko.hctm.MOD_ID
import net.dblsaiko.hctm.client.packet.onDebugNetUpdateResponse
import net.dblsaiko.hctm.common.packet.onDebugNetUpdateRequest
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.util.Identifier

object Packets {

  object Client {

    val DEBUG_NET_RESPONSE = Identifier(MOD_ID, "debug_net_recv")

    fun register() {
      ClientSidePacketRegistry.INSTANCE.register(DEBUG_NET_RESPONSE, ::onDebugNetUpdateResponse)
    }

  }

  object Server {

    val DEBUG_NET_REQUEST = Identifier(MOD_ID, "debug_net_req")

    fun register() {
      ServerSidePacketRegistry.INSTANCE.register(DEBUG_NET_REQUEST, ::onDebugNetUpdateRequest)
    }

  }

}