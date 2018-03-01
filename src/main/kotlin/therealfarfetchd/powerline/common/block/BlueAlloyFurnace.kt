package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.recipe.AlloyFurnaceRecipes
import therealfarfetchd.quacklib.common.api.recipe.ItemTemplate
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

@BlockDef(registerModels = false, creativeTab = ModID)
class BlueAlloyFurnace : BlockPoweredInventory(), ITickable {
  private var resultSlot
    get() = stacks[0]
    set(value) {
      stacks[0] = value
    }
  private val inputSlots get() = stacks.slice(1..9)

  private var facing: EnumFacing = EnumFacing.NORTH

  private var cookTime: Int = 0
  private var totalCookTime: Int = 0

  init {
    displayCL.addProperties(this::facing)
    worldCL.addProperties(this::cookTime, this::totalCookTime, this::facing)
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    if (placer != null) facing = placer.adjustedHorizontalFacing.opposite
  }

  override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
    return when (index) {
      0 -> false
      else -> true
    }
  }

  override fun update() {
    super.update()
    if (world.isServer) {
      if (hasPower) {
        val recipe = AlloyFurnaceRecipes.findRecipe(inputSlots)
          ?.let { it.first to it.second.makeStack() }
          ?.takeIf { resultSlot.isEmpty || resultSlot.isItemEqual(it.second) }
        val hasRecipe = recipe != null

        if (hasRecipe) {
          if (cookTime < totalCookTime) {
            cookTime++
          } else {
            if (totalCookTime != 0) {
              craftRecipe(recipe!!)
              cond.applyPower(-50.0)
            }
            cookTime = 0
            totalCookTime = 50
          }
        } else {
          cookTime = 0
          totalCookTime = 0
        }
      }

      if (worldCL.valuesChanged() or cond.cl.valuesChanged()) dataChanged()
      if (displayCL.valuesChanged()) {
        clientDataChanged(true)
        clientCL.valuesChanged() // clear clientCL changed flag
      } else if (clientCL.valuesChanged()) {
        clientDataChanged(false)
      }
    }
  }

  private fun craftRecipe(r: Pair<List<ItemTemplate>, ItemStack>) {
    val outStack = r.second

    if (resultSlot.isEmpty || resultSlot.isItemEqual(outStack)) {
      for (item in r.first) {
        var count = item.makeStack().count
        for (invItem in inputSlots) {
          if (item.isSameItem(invItem)) {
            val take = minOf(invItem.count, count)
            invItem.count -= take
            count -= take
          }
        }
      }
      if (resultSlot.isEmpty) resultSlot = outStack
      else resultSlot.also { it.count += outStack.count }
    }
  }

  override fun getField(id: Int): Int {
    return when (id) {
      0 -> cookTime
      1 -> totalCookTime
      else -> error("Out of range")
    }
  }

  override fun setField(id: Int, value: Int) {
    when (id) {
      0 -> cookTime = value
      1 -> totalCookTime = value
    }
  }

  override fun createContainer(inventory: InventoryPlayer, player: EntityPlayer): Container {
    return ContainerBlueAlloyFurnace(inventory, this)
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    facing = facing.rotateY()
    return true
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["F"] = facing.horizontalIndex
    if (target != DataTarget.Client) {
      nbt.ushort["CC"] = cookTime
      nbt.ushort["MC"] = totalCookTime
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getHorizontal(nbt.ubyte["F"])
    if (target != DataTarget.Client) {
      cookTime = nbt.ushort["CC"]
      totalCookTime = nbt.ushort["MC"]
    }
  }

  override fun getSlotsForFace(side: EnumFacing): IntArray {
    return when (side) {
      EnumFacing.DOWN -> intArrayOf(0) // fuel slot
      EnumFacing.UP -> (2..10).toList().toIntArray() // input slots
      EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST -> intArrayOf(1) // output slot
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state)
      .withProperty(PropFacing, facing)
      .withProperty(PropLit, totalCookTime != 0)
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    return when (capability) {
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> if (side != null) handler(side) as T else null
      else -> super.getCapability(capability, side)
    }
  }

  override fun getItem(): ItemStack = Item.makeStack()
  override fun getFieldCount(): Int = 2
  override fun getSizeInventory(): Int = 10

  override val material: Material = Material.ROCK
  override val blockType: ResourceLocation = ResourceLocation(ModID, "blue_alloy_furnace")
  override val properties: Set<IProperty<*>> = super.properties + PropFacing + PropLit

  companion object {
    val PropFacing = PropertyEnum.create("facing", EnumFacing::class.java, *EnumFacing.HORIZONTALS)
    val PropLit = PropertyBool.create("lit")

    val Block by WrapperImplManager.container(BlueAlloyFurnace::class)
    val Item by WrapperImplManager.item(BlueAlloyFurnace::class)
  }
}