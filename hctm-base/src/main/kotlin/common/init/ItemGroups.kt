package net.dblsaiko.hctm.common.init

import net.dblsaiko.hctm.MOD_ID
import net.dblsaiko.hctm.common.util.ext.makeStack
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier

object ItemGroups {

  val ALL: ItemGroup = FabricItemGroupBuilder.create(Identifier(MOD_ID, "all"))
    .icon { Items.SCREWDRIVER.makeStack() }
    .build()

}