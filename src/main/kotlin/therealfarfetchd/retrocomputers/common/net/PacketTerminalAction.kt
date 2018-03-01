package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.quacklib.common.api.extensions.getQBlock
import therealfarfetchd.quacklib.common.api.extensions.pmod
import therealfarfetchd.retrocomputers.common.block.Terminal

class PacketTerminalAction(var dim: Int, var pos: BlockPos, var key: Byte) : IMessage {

  @Suppress("unused") // this is used when the packet is received on the server
  constructor() : this(0, BlockPos(0, 0, 0), 0)

  override fun fromBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      dim = readInt()
      pos = readBlockPos()
      key = readByte()
    }
  }

  override fun toBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      writeInt(dim)
      writeBlockPos(pos)
      writeByte(key.toInt())
    }
  }

  object Handler : IMessageHandler<PacketTerminalAction, IMessage> {
    override fun onMessage(message: PacketTerminalAction, ctx: MessageContext): IMessage? {
      with(message) {
        val world = DimensionManager.getWorld(dim)
        val term = world.getQBlock(pos) as? Terminal
        if (term != null) {
          with(term) {
            if (kbHead + 1 pmod 16 != kbTail) {
              kbuf[kbHead] = message.key
              kbHead++
            }
          }
        }
      }
      return null
    }
  }
}