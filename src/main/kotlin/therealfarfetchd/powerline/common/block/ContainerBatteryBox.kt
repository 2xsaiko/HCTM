package therealfarfetchd.powerline.common.block

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import therealfarfetchd.powerline.common.api.item.IChargeItem
import therealfarfetchd.quacklib.common.api.qblock.IQBlockInventory

class ContainerBatteryBox(playerInv: InventoryPlayer, val inventory: IQBlockInventory) : Container() {

  init {
    this.addSlotToContainer(SlotChargeItem(inventory, 0, 134, 24))
    this.addSlotToContainer(SlotChargeItem(inventory, 1, 134, 55))

    // Player inventory
    for (i in 0 until 3) {
      for (j in 0 until 9) {
        addSlotToContainer(Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
      }
    }

    for (i in 0 until 9) {
      addSlotToContainer(Slot(playerInv, i, 8 + i * 18, 142))
    }
  }

  override fun addListener(listener: IContainerListener) {
    super.addListener(listener)
    listener.sendAllWindowProperties(this, inventory)
  }

  /**
   * Handle when the stack in slot `index` is shift-clicked. Normally this moves the stack between the player
   * inventory and the other inventory(s).
   */
  override fun transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack {
    var itemstack = ItemStack.EMPTY
    val slot = this.inventorySlots[index] ?: return itemstack
    val stack = slot.stack
    if (!stack.isEmpty) {
      itemstack = stack.copy()

      if (index < 2) {
        if (!this.mergeItemStack(stack, 2, 37, true)) {
          return ItemStack.EMPTY
        }
      } else {
        if (!this.mergeItemStack(stack, 0, 2, false)) {
          return ItemStack.EMPTY
        }
      }

      if (stack.isEmpty) {
        slot.putStack(ItemStack.EMPTY)
      } else {
        slot.onSlotChanged()
      }

      if (stack.count == itemstack.count) {
        return ItemStack.EMPTY
      }

      slot.onTake(playerIn, stack)
    }

    return itemstack
  }

  override fun canInteractWith(playerIn: EntityPlayer): Boolean = inventory.isUsableByPlayer(playerIn)

  class SlotChargeItem(inventoryIn: IInventory, index: Int, xPosition: Int, yPosition: Int) : Slot(inventoryIn, index, xPosition, yPosition) {
    override fun isItemValid(stack: ItemStack): Boolean = stack.item is IChargeItem
    override fun getSlotStackLimit(): Int = 1
  }
}