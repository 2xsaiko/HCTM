package therealfarfetchd.tubes.common.api.block.capability

import therealfarfetchd.tubes.common.api.item.PathedItem

interface ITubeFlow : ITubeInterface {
  fun getItems(): Set<PathedItem>
}