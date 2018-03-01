package therealfarfetchd.tubes.client

import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import therealfarfetchd.quacklib.client.registerModelBakery
import therealfarfetchd.tubes.common.Proxy
import therealfarfetchd.tubes.common.block.RedstoneTube
import therealfarfetchd.tubes.common.block.Tube

class Proxy : Proxy() {
  @SubscribeEvent
  fun registerModels(event: ModelRegistryEvent) {
    registerModelBakery(Tube.Block, Tube.Item, Tube.Bakery)
    registerModelBakery(RedstoneTube.Block, RedstoneTube.Item, RedstoneTube.Bakery)
  }
}