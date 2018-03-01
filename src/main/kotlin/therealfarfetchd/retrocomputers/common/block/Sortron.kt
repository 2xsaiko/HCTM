//package therealfarfetchd.retrocomputers.common.block
//
//import com.elytradev.teckle.api.capabilities.CapabilityWorldNetworkTile
//import com.elytradev.teckle.common.TeckleMod
//import com.elytradev.teckle.common.tile.inv.AdvancedItemStackHandler
//import com.elytradev.teckle.common.tile.inv.pool.AdvancedStackHandlerEntry
//import com.elytradev.teckle.common.tile.inv.pool.AdvancedStackHandlerPool
//import com.elytradev.teckle.common.worldnetwork.common.WorldNetworkDatabase
//import net.minecraft.block.material.Material
//import net.minecraft.block.properties.IProperty
//import net.minecraft.block.properties.PropertyBool
//import net.minecraft.block.state.IBlockState
//import net.minecraft.entity.EntityLivingBase
//import net.minecraft.item.EnumDyeColor
//import net.minecraft.item.ItemStack
//import net.minecraft.util.EnumFacing
//import net.minecraft.util.ITickable
//import net.minecraft.util.ResourceLocation
//import net.minecraftforge.common.capabilities.Capability
//import therealfarfetchd.powerline.common.api.PowerConductor
//import therealfarfetchd.powerline.common.api.block.capability.SimplePowerConnectable
//import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
//import therealfarfetchd.quacklib.common.api.extensions.isServer
//import therealfarfetchd.quacklib.common.api.extensions.makeStack
//import therealfarfetchd.quacklib.common.api.extensions.shr
//import therealfarfetchd.quacklib.common.api.extensions.unsigned
//import therealfarfetchd.quacklib.common.api.neighborSupport
//import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
//import therealfarfetchd.quacklib.common.api.util.BlockDef
//import therealfarfetchd.quacklib.common.api.util.ChangeListener
//import therealfarfetchd.quacklib.common.api.util.DataTarget
//import therealfarfetchd.quacklib.common.api.util.QNBTCompound
//import therealfarfetchd.retrocomputers.ModID
//import therealfarfetchd.retrocomputers.RetroComputers
//import therealfarfetchd.retrocomputers.common.api.block.capability.IBusConnectable
//import therealfarfetchd.retrocomputers.common.block.networktile.*
//import therealfarfetchd.retrocomputers.common.block.templates.Directional
//import therealfarfetchd.retrocomputers.common.util.ColoredItemStack
//import therealfarfetchd.retrocomputers.common.util.ItemHash
//import java.util.*
//import kotlin.reflect.KMutableProperty0
//
///**
// * Created by marco on 10.07.17.
// */
//@BlockDef(dependencies = "[teckle compat];[blutricity]", creativeTab = ModID)
//class Sortron : Directional(5), ITickable, SortronItemActions {
//
//  private val pulling: Boolean
//    get() = cooldown != 0
//
//  private var cooldown: Int = 0
//
//  var bufferData: AdvancedStackHandlerEntry? = null; private set
//  var bufferID: UUID? = null; private set
//  val buffer: AdvancedItemStackHandler
//    get() = bufferData!!.handler
//
//  var command: Byte = 0; private set
//  var quantity: Byte = 0
//  var slot: Short = 0; private set
//  var itemid: Int = 0; private set
//  var damage: Short = 0; private set
//  var maxdamage: Short = 0; private set
//  var outputColor: Byte = 0; private set
//  var inputColor: Byte = 0; private set
//
//  val cond = PowerConductor(neighborSupport())
//  val connectable = SimplePowerConnectable(cond)
//
//  val inputTile: NetworkTileSortronInput
//    get() = _inputTile!!
//  val outputTile: NetworkTileSortronOutput
//    get() = _outputTile!!
//  private var _inputTile: NetworkTileSortronInput? = null
//  private var _outputTile: NetworkTileSortronOutput? = null
//
//  private val clientCL: ChangeListener = ChangeListener(this::pulling)
//  private val worldCL: ChangeListener = ChangeListener(this::cooldown, this::command, this::quantity,
//    this::slot, this::itemid, this::damage, this::maxdamage, this::outputColor, this::inputColor)
//
//  val output: EnumFacing
//    get() = facing
//
//  val input: EnumFacing
//    get() = facing.opposite
//
//  override fun update() {
//    if (world.isServer) {
//      if (!buffer.stream().allMatch { it.isEmpty }) setCooldown()
//      for (i in 0 until buffer.slots) {
//        val stack = ColoredItemStack(buffer.getStackInSlot(i))
//        if (stack.stack.isEmpty) continue
//        val remaining = ejectToTube(stack)
//        if (remaining == null) buffer.setStackInSlot(i, ItemStack.EMPTY)
//        else buffer.setStackInSlot(i, ColoredItemStack.addColorInformation(remaining.stack, remaining.color))
//      }
//
//      if (command > 0) {
//        when (command.unsigned) {
//          1 -> {
//            // get inventory size
//            val itemHandler = getInventory()
//            if (itemHandler != null) {
//              slot = itemHandler.slots.toShort()
//              command = 0
//            } else {
//              slot = 0
//              command = -1
//            }
//          }
//          2 -> {
//            // get slot data
//            val itemHandler = getInventory()
//            val myslot = slot.unsigned.takeIf { it in 0 until (itemHandler?.slots ?: 0) }
//            if (itemHandler != null && myslot != null) {
//              val stack = itemHandler.getStackInSlot(myslot)
//              val hasSubtypes = stack.hasSubtypes
//              quantity = stack.count.toByte()
//              itemid = ItemHash.invoke(stack)
//              damage = if (hasSubtypes) 0 else stack.itemDamage.toShort()
//              maxdamage = if (hasSubtypes) 0 else stack.maxDamage.toShort()
//              command = 0
//            } else {
//              quantity = 0
//              itemid = 0
//              damage = 0
//              maxdamage = 0
//              command = -1
//            }
//          }
//          3 -> {
//            // pull
//            if (cooldown == 0) {
//              val itemHandler = getInventory()
//              val myslot = slot.unsigned.takeIf { it in 0 until (itemHandler?.slots ?: 0) }
//              val item = myslot?.let { itemHandler?.extractItem(it, 1, false) }?.takeUnless { it.isEmpty }
//              if (item != null) {
//                ejectToTube(ColoredItemStack(item, byte2DyeColor(outputColor)))
//                  ?.let { ejectToWorld(it) }
//                  .also { command = if (it == null) 0 else -1 }
//                  ?.let { ejectToInventory(it) }
//                  ?.let { ejectToBuffer(it) }
//                  ?.let { ejectToWorld(it, true) }
//                setCooldown()
//              } else command = -1
//            }
//          }
//          4 -> {
//            // match
//            // just clear the command if there's no more items, everything else is handled by network tile
//            if (quantity.unsigned == 0) command = 0
//          }
//        }
//      }
//      if (cooldown != 0) cooldown--
//      if (clientCL.valuesChanged()) clientDataChanged()
//      if (worldCL.valuesChanged()) dataChanged()
//    }
//  }
//
//  override fun peek(addr: Byte): Byte {
//    return when (addr.unsigned) {
//      0x00 -> command
//      0x01 -> quantity
//      0x02 -> slot.toByte()
//      0x03 -> (slot shr 8).toByte()
//      0x04 -> itemid.toByte()
//      0x05 -> (itemid shr 8).toByte()
//      0x06 -> (itemid shr 16).toByte()
//      0x07 -> (itemid shr 24).toByte()
//      0x08 -> damage.toByte()
//      0x09 -> (damage shr 8).toByte()
//      0x0A -> maxdamage.toByte()
//      0x0B -> (maxdamage shr 8).toByte()
//      0x0C -> outputColor
//      0x0D -> inputColor
//      else -> 0
//    }
//  }
//
//  override fun poke(addr: Byte, b: Byte) {
//    when (addr.unsigned) {
//      0x00 -> command = b
//      0x01 -> quantity = b
//      0x02 -> slot = ((slot.unsigned and 0xFF00) or b.unsigned).toShort()
//      0x03 -> slot = ((slot.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
//      0x04 -> itemid = ((itemid.unsigned and 0xFFFFFF00) or b.unsigned.toLong()).toInt()
//      0x05 -> itemid = ((itemid.unsigned and 0xFFFF00FF) or ((b.unsigned shl 8).toLong())).toInt()
//      0x06 -> itemid = ((itemid.unsigned and 0xFF00FFFF) or ((b.unsigned shl 16).toLong())).toInt()
//      0x07 -> itemid = ((itemid.unsigned and 0x00FFFFFF) or ((b.unsigned shl 24).toLong())).toInt()
//      0x08 -> damage = ((damage.unsigned and 0xFF00) or b.unsigned).toShort()
//      0x09 -> damage = ((damage.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
//      0x0A -> maxdamage = ((maxdamage.unsigned and 0xFF00) or b.unsigned).toShort()
//      0x0B -> maxdamage = ((maxdamage.unsigned and 0x00FF) or (b.unsigned shl 8)).toShort()
//      0x0C -> outputColor = b
//      0x0D -> inputColor = b
//    }
//  }
//
//  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
//    super.loadData(nbt, target)
//    cooldown = nbt.ubyte["Cooldown"]
//    if (target == DataTarget.Save) {
//      command = nbt.byte["Command"]
//      quantity = nbt.byte["Quantity"]
//      slot = nbt.short["Slot"]
//      itemid = nbt.int["ItemID"]
//      damage = nbt.short["Damage"]
//      maxdamage = nbt.short["MaxDamage"]
//      outputColor = nbt.byte["OutputColor"]
//      inputColor = nbt.byte["InputColor"]
//
//      cond.load(nbt.nbt["c"])
//
//      if (!prePlaced) {
//        if (loadNetworkTile(nbt, "InputID", input, this::_inputTile))
//          loadNetworkTile(nbt, "OutputID", output, this::_outputTile)
//      }
//    }
//  }
//
//  private fun <T : NetworkTileSortronBase> loadNetworkTile(nbt: QNBTCompound, tileIDKey: String, tileFace: EnumFacing, targetField: KMutableProperty0<T?>): Boolean {
//    var networkID: UUID? = if (tileIDKey in nbt) nbt.uuid[tileIDKey] else null
//    val dimID = nbt.int["DatabaseID"]
//    if (networkID == null) {
//      getNetworkAssistant().onNodePlaced(world, pos)
//      return false
//    } else {
//      val networkDB = WorldNetworkDatabase.getNetworkDB(dimID)
//      val any = networkDB.remappedNodes.keys.firstOrNull { pair -> pair.left == pos && pair.value == inputTile.capabilityFace }
//      if (any != null) {
//        networkID = networkDB.remappedNodes.remove(any)
//        RetroComputers.Logger.debug("Found a remapped network id for " + pos.toString() + " mapped id to " + networkID)
//      }
//
//      val network = WorldNetworkDatabase.getNetworkDB(dimID).get(networkID)
//      for (container in network.getNodeContainersAtPosition(pos)) {
//        if (container.facing == tileFace && container.networkTile != null) {
//          @Suppress("UNCHECKED_CAST")
//          targetField.set(container.networkTile as T)
//          break
//        }
//      }
//    }
//    return true
//  }
//
//  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
//    super.saveData(nbt, target)
//    nbt.ubyte["Cooldown"] = cooldown
//    if (target == DataTarget.Save) {
//      nbt.byte["Command"] = command
//      nbt.byte["Quantity"] = quantity
//      nbt.short["Slot"] = slot
//      nbt.int["ItemID"] = itemid
//      nbt.short["Damage"] = damage
//      nbt.short["MaxDamage"] = maxdamage
//      nbt.byte["OutputColor"] = outputColor
//      nbt.byte["InputColor"] = inputColor
//      if (bufferID != null) nbt.uuid["Buffer"] = bufferID!!
//
//      cond.save(nbt.nbt["c"])
//
//      if (!prePlaced) {
//        nbt.int["DatabaseID"] = world.provider.dimension
//        if (outputTile.node == null/* || inputTile.node == null*/)
//          getNetworkAssistant().onNodePlaced(world, pos)
//        nbt.uuid["InputID"] = inputTile.node.network.networkID
//        nbt.uuid["OutputID"] = outputTile.node.network.networkID
//      }
//    }
//  }
//
//  override fun validate() {
//    if (bufferID == null) {
//      if (bufferData == null) {
//        bufferData = AdvancedStackHandlerEntry(UUID.randomUUID(), world.provider.dimension, pos, AdvancedItemStackHandler(32))
//        AdvancedStackHandlerPool.getPool(world.provider.dimension).put(bufferData!!.id, bufferData)
//      }
//      bufferID = bufferData!!.id
//    } else {
//      bufferData = AdvancedStackHandlerPool.getPool(world.provider.dimension).get(bufferID)
//    }
//    if (this._inputTile == null)
//      this._inputTile = NetworkTileSortronInput(this)
//    if (this._outputTile == null)
//      this._outputTile = NetworkTileSortronOutput(this)
//
//    this.inputTile.bufferData = this.bufferData!!
//    this.inputTile.bufferID = this.bufferID!!
//
//    this.outputTile.bufferData = this.bufferData!!
//    this.outputTile.bufferID = this.bufferID!!
//
//    this.inputTile.otherTile = outputTile
//    this.outputTile.otherTile = inputTile
//  }
//
//  override fun applyProperties(state: IBlockState): IBlockState = super.applyProperties(state).withProperty(PropPulling, pulling)
//
//  override fun rotateBlock(axis: EnumFacing): Boolean {
//    if (super.rotateBlock(axis)) {
//      updateNetwork()
//      return true
//    }
//    return false
//  }
//
//  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
//    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
//    updateNetwork()
//  }
//
//  override fun onBreakBlock() {
//    if (CapabilityWorldNetworkTile.isPositionNetworkTile(world, pos, output)) {
//      getNetworkAssistant().onNodeBroken(world, pos)
//    }
//  }
//
//  override fun onNeighborTEChanged(side: EnumFacing) {
//    getNetworkAssistant().onNodeNeighbourChange(world, pos, pos.offset(side))
//  }
//
//  fun canAcceptItem(stack: ColoredItemStack): Boolean {
//    if (!buffer.stream().allMatch { it.isEmpty }) return false
//    if (quantity.unsigned == 0) return false
//    if (itemid != 0 && itemid != ItemHash(stack.stack)) return false
//    if (byte2DyeColor(inputColor) != stack.color) return false
//    val testItem = ColoredItemStack(stack.stack, byte2DyeColor(outputColor))
//    if (ejectToTube(testItem, true)?.let { it.stack.count == stack.stack.count } == true) return false
//    return true
//  }
//
//  override fun connectionForSide(f: EnumFacing?): IBusConnectable? {
//    return when {
//      f == null -> super.connectionForSide(EnumFacing.DOWN)
//      f.axis == facing.axis -> null
//      else -> super.connectionForSide(f)
//    }
//  }
//
//  fun setCooldown() {
//    cooldown = TeckleMod.CONFIG.sortingMachineCooldown
//  }
//
//  @Suppress("UNCHECKED_CAST")
//  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
//    return when (capability) {
//      CapabilityWorldNetworkTile.NETWORK_TILE_CAPABILITY -> when (side) {
//        output -> outputTile as T
//        input -> inputTile as T
//        else -> null
//      }
//      Capabilities.Connectable -> connectable as T
//      else -> super.getCapability(capability, side)
//    }
//  }
//
//  override val material: Material = Material.IRON
//
//  override fun getItem(): ItemStack = Item.makeStack()
//
//  override val properties: Set<IProperty<*>> = super.properties + PropPulling
//
//  override val blockType: ResourceLocation = ResourceLocation(ModID, "sortron")
//
//  private fun updateNetwork() {
//    val na = getNetworkAssistant()
//    na.onNodeBroken(world, pos)
//    na.onNodePlaced(world, pos)
//  }
//
//  companion object {
//    val PropPulling: PropertyBool = PropertyBool.create("pulling")
//
//    val Block = WrapperImplManager.getContainer(Sortron::class)
//    val Item = WrapperImplManager.getItem(Sortron::class)
//
//    fun byte2DyeColor(color: Byte): EnumDyeColor? {
//      if (color !in 1..16) return null
//      return EnumDyeColor.byMetadata(color - 1)
//    }
//
//    fun dyeColor2Byte(color: EnumDyeColor?): Byte {
//      if (color == null) return 0
//      return (color.metadata + 1).toByte()
//    }
//  }
//}