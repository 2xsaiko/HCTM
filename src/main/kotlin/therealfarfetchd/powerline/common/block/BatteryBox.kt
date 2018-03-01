package therealfarfetchd.powerline.common.block

import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.DefaultConductorConfiguration
import therealfarfetchd.powerline.common.api.item.IChargeItem
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

@BlockDef(registerModels = false, creativeTab = ModID)
class BatteryBox : BlockPoweredInventory(DefaultConductorConfiguration.BatteryBox) {
  private var dischargeSlot: ItemStack
    get() = stacks[0]
    set(value) {
      stacks[0] = value
    }
  private var chargeSlot: ItemStack
    get() = stacks[1]
    set(value) {
      stacks[1] = value
    }

  var charge: Int = 0

  init {
    clientCL.addProperties(this::hasPower)
    displayCL.addProperties(this::charge)
    displayCL.removeProperties(this::hasPower)
    worldCL.addProperties(this::charge)
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    if (stack != null) {
      charge = maxOf(0, minOf(stack.metadata, MaxCharge))
    }
  }

  val canCharge: Boolean
    get() = cond.voltage >= ChargeLimit && charge < MaxCharge

  val canDischarge: Boolean
    get() = cond.voltage <= DischargeLimit && charge > 0

  override fun update() {
    if (world.isServer) {
      if (canCharge) {
        val chargeAmt = minOf(MaxCharge - charge, (cond.voltage.toInt() - ChargeLimit) * 10, 1000) / 10
        cond.applyPower(-chargeAmt.toDouble())
        charge += chargeAmt
      } else if (canDischarge) {
        val dischargeAmt = minOf(charge, (DischargeLimit - cond.voltage.toInt()) * 10, 1000) / 10
        cond.applyPower(dischargeAmt.toDouble())
        charge -= dischargeAmt
      }
    }

    super.update()
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ushort["ch"] = charge
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    charge = nbt.ushort["ch"]
  }

  override fun getSlotsForFace(side: EnumFacing): IntArray {
    return when (side) {
      EnumFacing.DOWN -> intArrayOf(1)
      EnumFacing.UP   -> intArrayOf(0)
      EnumFacing.NORTH,
      EnumFacing.SOUTH,
      EnumFacing.WEST,
      EnumFacing.EAST -> intArrayOf()
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state).withProperty(PropCharge, getChargeForDisplay(charge))
  }

  override fun createContainer(inventory: InventoryPlayer, player: EntityPlayer): Container = ContainerBatteryBox(inventory, this)
  override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean = stack.item is IChargeItem
  override fun getItem(): ItemStack = Item.makeStack(meta = charge)
  override fun getSizeInventory(): Int = 2

  override val material: Material = Material.ROCK
  override val properties: Set<IProperty<*>> = super.properties + PropCharge
  override val soundType: SoundType = SoundType.WOOD
  override val useNeighborBrightness: Boolean = true // fix for strange shadow bug
  override val blockType: ResourceLocation = ResourceLocation(ModID, "battery_box")

  companion object {
    val Block by WrapperImplManager.container(BatteryBox::class)
    val Item by WrapperImplManager.item(BatteryBox::class)

    init {
      WrapperImplManager.itemMod(BatteryBox::class) {
        hasSubtypes = true
      }
    }

    val MaxCharge = 8000
    val ChargeLimit = 90
    val DischargeLimit = 80

    fun getChargeForDisplay(c: Int): Int = minOf(maxOf(0, c / 750), 10)

    @Suppress("UNCHECKED_CAST")
    val PropCharge = PropertyInteger.create("charge", 0, 10)
  }
}