package therealfarfetchd.powerline.common.block

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import therealfarfetchd.quacklib.common.api.qblock.IQBlockInventory
import therealfarfetchd.quacklib.common.block.ContainerAlloyFurnace.SlotOutput

class ContainerBlueAlloyFurnace(playerInv: InventoryPlayer, val inventory: IQBlockInventory) : Container() {
  var cookTime: Int = 0; private set
  var totalCookTime: Int = 0; private set

  init {
    this.addSlotToContainer(SlotOutput(playerInv.player, inventory, 0, 134, 34))
    for (i in 0 until 3) {
      for (j in 0 until 3) {
        this.addSlotToContainer(Slot(inventory, 1 + j + i * 3, 44 + j * 18, 16 + i * 18))
      }
    }

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
   * Looks for changes made in the container, sends them to every listener.
   */
  override fun detectAndSendChanges() {
    super.detectAndSendChanges()

    for (i in this.listeners.indices) {
      val icontainerlistener = this.listeners[i]

      if (this.cookTime != inventory.getField(0)) {
        icontainerlistener.sendWindowProperty(this, 0, inventory.getField(0))
      }

      if (this.totalCookTime != inventory.getField(1)) {
        icontainerlistener.sendWindowProperty(this, 1, inventory.getField(1))
      }
    }

    this.cookTime = inventory.getField(0)
    this.totalCookTime = inventory.getField(1)
  }

  @SideOnly(Side.CLIENT)
  override fun updateProgressBar(id: Int, data: Int) {
    inventory.setField(id, data)
    when (id) {
      0 -> cookTime = data
      1 -> totalCookTime = data
    }
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

      if (index < 10) {
        if (!this.mergeItemStack(stack, 10, 45, true)) {
          return ItemStack.EMPTY
        }
      } else {
        if (!this.mergeItemStack(stack, 1, 10, false)) {
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
}