package therealfarfetchd.powerline.common.items

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import therealfarfetchd.powerline.common.block.BlockPowered

class PoweredItemHandler(val powerProvider: BlockPowered, val wrapped: IItemHandlerModifiable) : IItemHandlerModifiable by wrapped {
  override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
    return if (powerProvider.hasPower) {
      val res = wrapped.insertItem(slot, stack, simulate)
      if (!simulate) {
        val transferred = maxOf(0, stack.count - res.count)
        val powerCost = 5.0 + transferred * 25.0
        powerProvider.cond.applyPower(-powerCost)
      }
      res
    } else stack
  }

  override fun getStackInSlot(slot: Int) =
    if (powerProvider.hasPower) wrapped.getStackInSlot(slot) else ItemStack.EMPTY

  override fun setStackInSlot(slot: Int, stack: ItemStack) {
    if (!powerProvider.hasPower) error("Block is out of power, blocking setStackInSlot!")
    val powerCost = 5.0 + stack.count * 25.0
    powerProvider.cond.applyPower(-powerCost)
    wrapped.setStackInSlot(slot, stack)
  }

  override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
    return if (powerProvider.hasPower) {
      val res = wrapped.extractItem(slot, amount, simulate)
      if (!simulate) {
        val powerCost = 5.0 + res.count * 25.0
        powerProvider.cond.applyPower(-powerCost)
      }
      res
    } else ItemStack.EMPTY
  }
}