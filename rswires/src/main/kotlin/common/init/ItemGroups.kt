package therealfarfetchd.rswires.common.init

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import therealfarfetchd.hctm.common.util.ext.makeStack
import therealfarfetchd.rswires.ModID

object ItemGroups {

  val All: ItemGroup = FabricItemGroupBuilder.create(Identifier(ModID, "all"))
    .icon { Items.RedAlloyWire.makeStack() }
    .build()

}