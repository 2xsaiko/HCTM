package therealfarfetchd.tubes.common.api.item

import therealfarfetchd.tubes.common.api.block.capability.ITubeInterface

interface NodePath {
  val path: List<ITubeInterface>
  var currentNode: ITubeInterface

  val atTarget: Boolean

  fun next()

  fun reverse()
}