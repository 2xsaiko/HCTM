package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.retrocomputers.common.block.Rack

class PacketComponentUpdate(var pos: BlockPos, var slotId: Int, var data: List<Byte>) : IMessage {
  @Suppress("unused")
  constructor() : this(BlockPos.ORIGIN, 0, emptyList())

  override fun fromBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pos = pb.readBlockPos()
    slotId = pb.readVarInt()
    data = pb.readByteArray().toList()
  }

  override fun toBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pb.writeBlockPos(pos)
    pb.writeVarInt(slotId)
    pb.writeByteArray(data.toByteArray())
  }

  object Handler : IMessageHandler<PacketComponentUpdate, Nothing> {
    override fun onMessage(message: PacketComponentUpdate, ctx: MessageContext): Nothing? {
      val mc = Minecraft.getMinecraft()
      mc.addScheduledTask {
        val world = mc.world
        val te = world.getTileEntity(message.pos) as? Rack.Tile ?: return@addScheduledTask
        val component = te.container.getComponent(message.slotId)
        val pb = PacketBuffer(Unpooled.wrappedBuffer(message.data.toByteArray()))
        component.readClientData(pb)
      }
      return null
    }
  }
}