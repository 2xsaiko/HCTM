package therealfarfetchd.retrocomputers.common.api.bus

/**
 * Created by marco on 24.06.17.
 * Can be anything, cables, peripherals, whatever. You should probably implement this in your TE class.
 */
//interface IBusObject {
//
//  /**
//   * @return the objects connected to this object
//   */
//  val neighbors: Set<IBusObject>
//
//  /**
//   * @return all objects connected together
//   */
//  fun resolveNetwork(): Set<IBusObject> {
//    var set = emptySet<IBusObject>()
//    var newest = setOf(this)
//    while (newest.isNotEmpty()) {
//      val newer = newest.flatMap { it.neighbors } - set
//      set += newer
//      newest = newer.toSet()
//    }
//    return set
//  }
//
//  companion object {
//    fun defaultNeighborsImpl(op: (EnumFacing) -> IBusConnectable?, container: TileEntity): Set<IBusObject> {
//      return EnumFacing.values().flatMap { face -> EnumFacing.values().map { face to it } }
//          .filter { op(it.first) != null }
//          .map { container.connAt(it.first, it.second).first }
//          .filterNotNull()
//          .flatMap { it.elements }
//          .map { it.second }
//          .toSet()
//    }
//  }
//}