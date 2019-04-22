package therealfarfetchd.retrocomputers.common.packet.client

import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.world.dimension.DimensionType
import therealfarfetchd.retrocomputers.client.block.wire.ClientNetworkState

fun onDebugNetUpdateResponse(context: PacketContext, buffer: PacketByteBuf) {
  val dim = Identifier(buffer.readString())
  val type = DimensionType.byId(dim)!!

  val tag = buffer.readCompoundTag()!!
  ClientNetworkState.update(type, tag)
}