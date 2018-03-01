package therealfarfetchd.retrocomputers.common.extensions

//fun TileEntity.connAt(side: EnumFacing, base: EnumFacing): Pair<IBusConnectable?, EnumWireConnection> {
//  val cap: IBusConnectable = getCapability(CompNet, side) ?: return null to None
//  var value: Pair<IBusConnectable, EnumWireConnection>? = null
//  val thisTE = (((this as? QBContainerTileMultipart)?.qbmp?.actualWorld) ?: world).getTileEntity(pos)
//  if (thisTE is TileMultipartContainer) {
//    thisTE.getPartTile(EnumFaceSlot.fromFace(side)).ifPresent {
//      val tile = it.tileEntity
//      if (tile is QBContainerTile) {
//        val cable = tile.qb
//        if (cable is RibbonCable) {
//          value = cable.getCapability(CompNet, base.opposite)?.to(Internal)
//        }
//      }
//    }
//  }
//  // check if neighbor block has capability
//  if (value == null) {
//    val te = world.getTileEntity(pos.offset(side))
//    if (te != null && te.hasCapability(CompNet, side.opposite)) {
//      val capability = te.getCapability(CompNet, side.opposite)!!
//      if (cap.elements.map { it.first }.any { capability.at(it) != null }) value = capability to External
//    }
//  }
//  // check if there's a block around the corner, this will only be valid if at least one is a cable
//  if (value == null) {
//    val te = world.getTileEntity(pos.offset(side).offset(base))
//    if (te != null && te.hasCapability(CompNet, base.opposite)) {
//      val capability = te.getCapability(CompNet, base.opposite)!!
//      if (cap.allowCornerConnections || (capability as? JoinedCablePort)?.unwrapped?.any { it.allowCornerConnections }
//          ?: capability.allowCornerConnections && cap.elements.any { capability.at(it.first.flip(side, base)) != null })
//        value = capability to Corner
//    }
//  }
//  return value ?: null to None
//}