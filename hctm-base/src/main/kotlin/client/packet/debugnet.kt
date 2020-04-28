package net.dblsaiko.hctm.client.packet

import net.dblsaiko.hctm.client.wire.ClientNetworkState
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.world.dimension.DimensionType

fun onDebugNetUpdateResponse(context: PacketContext, buffer: PacketByteBuf) {
  val dim = Identifier(buffer.readString())
  val type = DimensionType.byId(dim)!!

  val tag = buffer.readCompoundTag()!!

  context.taskQueue.execute {
    ClientNetworkState.update(type, tag)
  }
}