package net.dblsaiko.hctm.client.wire

import io.netty.buffer.Unpooled
import net.dblsaiko.hctm.common.init.Packets
import net.dblsaiko.hctm.common.wire.WireNetworkController
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

object ClientNetworkState {

  private val caches = mutableMapOf<DimensionType, Entry>()

  fun request(world: World): WireNetworkController? {
    if (!world.isClient) error("Yeah let's not do that.")

    if (caches[world.dimension.type]?.isExpired() != false) {
      val buf = PacketByteBuf(Unpooled.buffer())
      buf.writeString(Registry.DIMENSION_TYPE.getId(world.dimension.type)!!.toString())
      ClientSidePacketRegistry.INSTANCE.sendToServer(Packets.Server.DebugNetRequest, buf)
    }

    return caches[world.dimension.type]?.controller
  }

  fun update(dt: DimensionType, tag: CompoundTag) {
    caches[dt] = Entry(WireNetworkController.fromTag(tag))
  }

}

private data class Entry(val controller: WireNetworkController, val created: Long = utime()) {
  fun isExpired() = utime() - created > 1
}

private fun utime() = System.currentTimeMillis() / 1000