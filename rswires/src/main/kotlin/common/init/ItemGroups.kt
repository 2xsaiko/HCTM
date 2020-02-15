package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.rswires.MOD_ID
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier

object ItemGroups {

  val ALL: ItemGroup = FabricItemGroupBuilder.create(Identifier(MOD_ID, "all"))
    .icon { Items.RED_ALLOY_WIRE.makeStack() }
    .build()

}