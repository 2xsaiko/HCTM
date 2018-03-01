package therealfarfetchd.powerline.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import therealfarfetchd.powerline.common.api.DefaultConductorConfiguration
import therealfarfetchd.powerline.common.api.IConductorConfiguration
import therealfarfetchd.quacklib.common.api.extensions.spawnAt
import therealfarfetchd.quacklib.common.api.qblock.IQBlockInventory
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

abstract class BlockPoweredInventory(cconf: IConductorConfiguration = DefaultConductorConfiguration.LVDevice) : BlockPowered(cconf), IQBlockInventory {
  protected val stacks = Array(sizeInventory, { ItemStack.EMPTY })

  override var customName: String? = null

  var clientVLevel: Int = 0
  var clientCLevel: Int = 0

  init {
    clientCL.addProperties(cond::voltage, cond::chargeLevel)
    worldCL.addProperties(this::stacks)
  }

  override fun getStack(index: Int): ItemStack = stacks[index]

  override fun setStack(index: Int, stack: ItemStack) {
    stacks[index] = stack
  }

  override fun onBreakBlock() {
    super.onBreakBlock()
    for (stack in stacks) stack.spawnAt(world, pos)
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    customName?.also { nbt.string["CN"] = it }
    if (target != DataTarget.Client) {
      for ((i, item) in stacks.withIndex())
        item.writeToNBT(nbt.nbt["I$i"].self)
    } else {
      nbt.ubyte["v"] = (cond.voltage / 2).toInt()
      nbt.ubyte["c"] = cond.chargeLevel
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if ("CN" in nbt) customName = nbt.string["CN"]
    if (target != DataTarget.Client) {
      for (i in stacks.indices)
        stacks[i] = ItemStack(nbt.nbt["I$i"].self)
    } else {
      clientVLevel = nbt.ubyte["v"] * 2
      clientCLevel = nbt.ubyte["c"]
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    return when (capability) {
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> if (side != null && getSlotsForFace(side).isNotEmpty()) handler(side) as T else null
      else -> super.getCapability(capability, side)
    }
  }
}