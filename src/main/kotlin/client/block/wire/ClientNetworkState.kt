package therealfarfetchd.retrocomputers.client.block.wire

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import therealfarfetchd.retrocomputers.common.block.wire.WireNetworkController
import therealfarfetchd.retrocomputers.common.init.Packets
import java.awt.Color
import java.util.*
import kotlin.streams.asSequence

object ClientNetworkState {

  private val caches = mutableMapOf<DimensionType, Entry>()

  fun request(world: World): WireNetworkController? {
    if (!world.isClient) error("Yeah let's not do that.")

    if (caches[world.dimension.type]?.isExpired() != false) {
      val buf = PacketByteBuf(Unpooled.buffer())
      buf.writeString(Registry.DIMENSION.getId(world.dimension.type)!!.toString())
      ClientSidePacketRegistry.INSTANCE.sendToServer(Packets.Server.DebugNetRequest, buf)
    }

    return caches[world.dimension.type]?.controller
  }

  fun update(dt: DimensionType, tag: CompoundTag) {
    caches += dt to Entry(WireNetworkController.fromTag(tag))
  }

  fun getNetworkColor(id: UUID): Color {
    val rnd = Random(id.leastSignificantBits xor id.mostSignificantBits)
    val (r, g, b) = rnd.doubles().asSequence().take(3).toList()
    return Color(r.toFloat(), g.toFloat(), b.toFloat())
  }

}

private data class Entry(val controller: WireNetworkController, val created: Long = utime()) {
  fun isExpired() = utime() - created > 1000
}

private fun utime() = System.currentTimeMillis() / 1000