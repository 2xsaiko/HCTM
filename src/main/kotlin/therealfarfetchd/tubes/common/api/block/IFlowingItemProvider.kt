package therealfarfetchd.tubes.common.api.block

import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.tubes.common.api.item.ColoredItemStack

interface IFlowingItemProvider {
  fun getItems(): Set<Pair<ColoredItemStack, Vec3>>
}