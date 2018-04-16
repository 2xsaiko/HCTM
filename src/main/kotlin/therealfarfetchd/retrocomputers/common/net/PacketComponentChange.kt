package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.retrocomputers.RetroComputers.Logger
import therealfarfetchd.retrocomputers.common.api.component.ComponentRegistry
import therealfarfetchd.retrocomputers.common.block.Rack

class PacketComponentChange(var pos: BlockPos, var slotId: Int, var type: ResourceLocation) : IMessage {
  @Suppress("unused")
  constructor() : this(BlockPos.ORIGIN, 0, ResourceLocation("null"))

  override fun fromBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pos = pb.readBlockPos()
    slotId = pb.readVarInt()
    type = ResourceLocation(pb.readString(255))
  }

  override fun toBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pb.writeBlockPos(pos)
    pb.writeVarInt(slotId)
    pb.writeString(type.toString())
  }

  object Handler : IMessageHandler<PacketComponentChange, Nothing> {
    override fun onMessage(message: PacketComponentChange, ctx: MessageContext): Nothing? {
      val mc = Minecraft.getMinecraft()
      mc.addScheduledTask {
        val world = mc.world
        val te = world.getTileEntity(message.pos) as? Rack.Tile ?: return@addScheduledTask
        val newComponent = ComponentRegistry.create(message.type)
        if (newComponent == null) Logger.warn("Unknown type '${message.type}', setting null")
        te.container.setComponent(message.slotId, newComponent)
      }
      return null
    }
  }
}