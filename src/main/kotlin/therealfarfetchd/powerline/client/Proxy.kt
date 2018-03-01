package therealfarfetchd.powerline.client

import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import therealfarfetchd.powerline.client.model.*
import therealfarfetchd.powerline.common.Proxy
import therealfarfetchd.powerline.common.block.*
import therealfarfetchd.powerline.common.item.ItemSteelTube
import therealfarfetchd.powerline.common.item.ItemSteelTubeSmall
import therealfarfetchd.quacklib.client.registerModelBakery

class Proxy : Proxy() {
  @SubscribeEvent
  fun registerModels(event: ModelRegistryEvent) {
    registerModelBakery(LVPowerline.Block, LVPowerline.Item, LVPowerline.Bakery)
    registerModelBakery(HVPowerline.Block, HVPowerline.Item, HVPowerline.Bakery)
    registerModelBakery(BatteryBox.Block, BatteryBox.Item, ModelBatteryBox)
    registerModelBakery(FluidPipe::class, FluidPipe.Block, FluidPipe.Item, ModelFluidPipe)
    registerModelBakery(Pump::class, Pump.Block, Pump.Item, ModelPump)
    registerModelBakery(Grate.Block, Grate.Item, ModelGrate)
    registerModelBakery(BlueAlloyFurnace.Block, BlueAlloyFurnace.Item, ModelBlueAlloyFurnace)
    registerModelBakery(Fabricator::class, Fabricator.Block, Fabricator.Item, ModelFabricator)
    registerModelBakery(PlayerLink::class, PlayerLink.Block, PlayerLink.Item, ModelPlayerLink)
    registerModelBakery(ItemSteelTube, ModelSteelTube)
    registerModelBakery(ItemSteelTubeSmall, ModelSteelTubeSmall)
  }
}