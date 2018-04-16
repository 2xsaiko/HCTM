package therealfarfetchd.retrocomputers.common.api.component

import io.netty.buffer.Unpooled
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import therealfarfetchd.quacklib.common.api.extensions.sendToAllWatching
import therealfarfetchd.quacklib.common.api.extensions.spawnAt
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.api.component.ComponentRender
import therealfarfetchd.retrocomputers.common.net.PacketComponentUpdate

interface Component {
  var container: ComponentContainer

  fun update() {}

  fun onClicked(box: Int, player: EntityPlayer, hand: EnumHand): Boolean

  fun getBoundingBoxes(): List<AxisAlignedBB> = listOf(AxisAlignedBB(0.0, 0.0, 0.0, 0.75, 0.375, 1.0))

  fun saveData(nbt: QNBTCompound)

  fun loadData(nbt: QNBTCompound)

  fun sendDataToClient(context: PacketSendContext)

  fun readClientData(buf: PacketBuffer)

  fun onExtract() {
    getItem().spawnAt(container.world, container.pos.offset(container.facing.opposite))
  }

  fun getItem(): ItemStack

  @SideOnly(Side.CLIENT)
  fun getRenderer(): ComponentRender<*>
}

interface ComponentBusAware {
  val busId: Byte

  fun poke(addr: Byte, b: Byte)

  fun peek(addr: Byte): Byte
}

interface PacketSendContext {
  fun sendPacket(op: PacketBuffer.() -> Unit)

  companion object {
    operator fun invoke(handle: (PacketBuffer) -> Unit) = object : PacketSendContext {
      override fun sendPacket(op: PacketBuffer.() -> Unit) {
        PacketBuffer(Unpooled.buffer()).also(op).also(handle)
      }
    }
  }
}

fun Component.sendPacket(op: PacketBuffer.() -> Unit) {
  val buf = PacketBuffer(Unpooled.buffer()).also(op)
  RetroComputers.Net.sendToAllWatching(PacketComponentUpdate(container.pos, container.getSlotId(this)!!,
    buf.array().slice(buf.arrayOffset() until buf.arrayOffset() + buf.writerIndex())), container.world.provider.dimension, container.pos)
}