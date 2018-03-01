package therealfarfetchd.tubes.common.api.item

import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

class ColoredItemStack(val stack: ItemStack, val color: EnumDyeColor?) {
  constructor(nbt: QNBTCompound) : this(
    ItemStack(nbt.nbt["Item"].self),
    nbt.ubyte["Color"].takeIf { it in 0..15 }?.let { EnumDyeColor.byMetadata(it) }
  )

  var count: Int
    get() = stack.count
    set(value) {
      stack.count = value
    }

  val isEmpty: Boolean
    get() = stack.isEmpty

  fun withColor(c: EnumDyeColor? = null) = ColoredItemStack(stack.copy(), c)

  fun takeSome(count: Int): Pair<ColoredItemStack, ColoredItemStack> {
    val take = maxOf(0, minOf(count, this.count))
    val stackA = withCount(take)
    val stackB = withCount(this.count - stackA.count)
    return stackA to stackB
  }

  fun withCount(count: Int): ColoredItemStack {
    val s = copy()
    s.count = maxOf(0, minOf(count, stack.maxStackSize))
    return s
  }

  fun copy() = ColoredItemStack(stack.copy(), color)

  fun save(nbt: QNBTCompound) {
    nbt.ubyte["Color"] = color?.metadata ?: 16
    stack.writeToNBT(nbt.nbt["Item"].self)
  }
}

fun ItemStack.withColor(c: EnumDyeColor? = null) = ColoredItemStack(copy(), c)