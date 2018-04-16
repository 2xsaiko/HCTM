package therealfarfetchd.retrocomputers.common.component

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumHand
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.client.render.component.RenderDummy
import therealfarfetchd.retrocomputers.common.api.component.Component
import therealfarfetchd.retrocomputers.common.api.component.ComponentContainer
import therealfarfetchd.retrocomputers.common.api.component.PacketSendContext
import therealfarfetchd.retrocomputers.common.item.ItemDevice

class ComponentDummy : Component {
  override lateinit var container: ComponentContainer

  override fun onClicked(box: Int, player: EntityPlayer, hand: EnumHand): Boolean {
    val stack = player.getHeldItem(hand) ?: return false
    val item = stack.item as? ItemDevice ?: return false
    if (container.world.isServer) {
      container.setComponent(container.getSlotId(this)!!, item.create(stack))
      stack.shrink(1)
    }
    return true
  }

  override fun saveData(nbt: QNBTCompound) {}

  override fun loadData(nbt: QNBTCompound) {}

  override fun sendDataToClient(context: PacketSendContext) {}

  override fun readClientData(buf: PacketBuffer) {}

  override fun getItem(): ItemStack = ItemStack.EMPTY

  override fun getRenderer() = RenderDummy
}