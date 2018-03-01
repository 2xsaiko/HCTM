package therealfarfetchd.rswires.client

import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import therealfarfetchd.quacklib.client.registerModelBakery
import therealfarfetchd.rswires.client.model.ModelLamp
import therealfarfetchd.rswires.common.Proxy
import therealfarfetchd.rswires.common.block.*

class Proxy : Proxy() {
  @SubscribeEvent
  fun registerModels(event: ModelRegistryEvent) {
    registerModelBakery(RedAlloyWire.Block, RedAlloyWire.Item, RedAlloyWire.Bakery)
    registerModelBakery(InsulatedWire.Block, InsulatedWire.Item, InsulatedWire.Bakery)
    registerModelBakery(BundledCable.Block, BundledCable.Item, BundledCable.Bakery)
    registerModelBakery(Lamp, Lamp.Item, ModelLamp)
    registerModelBakery(LampOn, null, ModelLamp)
  }
}