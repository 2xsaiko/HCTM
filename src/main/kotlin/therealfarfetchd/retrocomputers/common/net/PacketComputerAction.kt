package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.quacklib.common.api.extensions.getQBlock
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.retrocomputers.common.block.Computer

class PacketComputerAction(var dim: Int, var pos: BlockPos, var act: Byte, var data: Byte) : IMessage {

  @Suppress("unused") // this is used when the packet is received on the server
  constructor() : this(0, BlockPos(0, 0, 0), 0, 0)

  override fun fromBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      dim = readInt()
      pos = readBlockPos()
      act = readByte()
      data = readByte()
    }
  }

  override fun toBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      writeInt(dim)
      writeBlockPos(pos)
      writeByte(act.toInt())
      writeByte(data.toInt())
    }
  }

  object Handler : IMessageHandler<PacketComputerAction, IMessage> {
    override fun onMessage(message: PacketComputerAction, ctx: MessageContext): IMessage? {
      with(message) {
        val world = DimensionManager.getWorld(dim)
        val comp = world.getQBlock(pos) as? Computer
        if (comp != null) {
          when (act.unsigned) {
            0x00 -> comp.running = false
            0x01 -> comp.running = true

            0x02 -> comp.cpu?.reset(false)
            0x03 -> comp.cpu?.reset(true)

            0x04 -> comp.diskAddr = data
            0x05 -> comp.termAddr = data

            0x06 -> comp.cpu?.also { it.error = false }
          }
        }
      }
      return null
    }
  }
}