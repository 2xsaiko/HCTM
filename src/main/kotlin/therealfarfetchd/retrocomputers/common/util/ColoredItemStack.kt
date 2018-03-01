//package therealfarfetchd.retrocomputers.common.util
//
//import com.elytradev.teckle.common.worldnetwork.common.WorldNetworkTraveller
//import com.google.common.collect.ImmutableMap
//import net.minecraft.item.EnumDyeColor
//import net.minecraft.item.ItemStack
//import net.minecraft.nbt.NBTBase
//import net.minecraft.nbt.NBTTagCompound
//import net.minecraft.nbt.NBTTagInt
//import therealfarfetchd.quacklib.common.api.extensions.unsigned
//import therealfarfetchd.quacklib.common.api.util.QNBTCompound
//
//class ColoredItemStack(val stack: ItemStack, val color: EnumDyeColor?) {
//
//  constructor(traveller: WorldNetworkTraveller) : this(
//    ItemStack(traveller.data.getCompoundTag("stack")),
//    if (traveller.data.hasKey("colour")) EnumDyeColor.byMetadata(traveller.data.getInteger("colour")) else null
//  )
//
//  constructor(stack: ItemStack) : this(removeColorInformation(stack), getColorInformation(stack))
//
//  fun writeToTraveller(traveller: WorldNetworkTraveller) {
//    traveller.data.setTag("stack", stack.serializeNBT())
//    if (color == null) traveller.data.removeTag("colour")
//    else traveller.data.setInteger("colour", color.metadata)
//  }
//
//  fun createData(): ImmutableMap<String, NBTBase> {
//    if (color == null) return ImmutableMap.of()
//    else return ImmutableMap.of("colour", NBTTagInt(color.metadata))
//  }
//
//  override fun equals(other: Any?): Boolean {
//    if (other !is ColoredItemStack) return false
//    if (color != other.color) return false
//    return ItemStack.areItemStacksEqual(stack, other.stack)
//  }
//
//  override fun hashCode(): Int {
//    var result = stack.hashCode()
//    result = 31 * result + (color?.hashCode() ?: 0)
//    return result
//  }
//
//  companion object {
//    fun addColorInformation(stackIn: ItemStack, color: EnumDyeColor?): ItemStack {
//      val stack = stackIn.copy()
//      if (!stack.hasTagCompound()) stack.tagCompound = NBTTagCompound()
//      val itemNbt = QNBTCompound(stack.tagCompound!!)
//      val data = itemNbt.nbt["\$ColorData"]
//      data.self.removeTag("color")
//      color?.also { data.ubyte["color"] = it.metadata }
//      return stack
//    }
//
//    fun getColorInformation(stack: ItemStack): EnumDyeColor? {
//      if (!stack.hasTagCompound()) return null
//      val nbt = stack.tagCompound!!
//      val data = nbt.getCompoundTag("\$ColorData")
//      if (!data.hasKey("color")) return null
//      return EnumDyeColor.byMetadata(data.getByte("color").unsigned)
//    }
//
//    fun removeColorInformation(stackIn: ItemStack): ItemStack {
//      val stack = stackIn.copy()
//      if (!stack.hasTagCompound()) return stack
//      val nbt = stack.tagCompound!!
//      if (nbt.hasKey("\$ColorData")) {
//        nbt.removeTag("\$ColorData")
//      }
//      if (nbt.hasNoTags()) stack.tagCompound = null
//      return stack
//    }
//  }
//}