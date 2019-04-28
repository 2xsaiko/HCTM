package therealfarfetchd.hctm.common.packet

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.world.dimension.DimensionType
import therealfarfetchd.hctm.common.init.Packets
import therealfarfetchd.hctm.common.wire.getWireNetworkState

fun onDebugNetUpdateRequest(context: PacketContext, buffer: PacketByteBuf) {
  val dim = Identifier(buffer.readString())
  val world = (context.player as ServerPlayerEntity).getServer()!!.getWorld(DimensionType.byId(dim))
  val wns = world.getWireNetworkState()
  val tag = wns.toTag(CompoundTag())

  val out = PacketByteBuf(Unpooled.buffer())
  out.writeString(dim.toString())
  out.writeCompoundTag(tag)
  ServerSidePacketRegistry.INSTANCE.sendToPlayer(context.player, Packets.Client.DebugNetResponse, out)
}