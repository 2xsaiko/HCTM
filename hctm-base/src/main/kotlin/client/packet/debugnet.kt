package therealfarfetchd.hctm.client.packet

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.world.dimension.DimensionType
import therealfarfetchd.hctm.client.wire.ClientNetworkState

fun onDebugNetUpdateResponse(context: PacketContext, buffer: PacketByteBuf) {
  val dim = Identifier(buffer.readString())
  val type = DimensionType.byId(dim)!!

  val tag = buffer.readCompoundTag()!!

  context.taskQueue.execute {
    ClientNetworkState.update(type, tag)
  }
}