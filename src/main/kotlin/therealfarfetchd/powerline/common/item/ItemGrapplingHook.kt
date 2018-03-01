package therealfarfetchd.powerline.common.item

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.EnumAction
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import therealfarfetchd.powerline.ModID
import therealfarfetchd.quacklib.common.api.util.ItemDef

@ItemDef
object ItemGrapplingHook : Item() {

  init {
    registryName = ResourceLocation(ModID, "grappling_hook")

    maxStackSize = 1
  }

  override fun onBlockStartBreak(itemstack: ItemStack?, pos: BlockPos?, player: EntityPlayer?): Boolean {
    return true
  }

  override fun canDestroyBlockInCreative(world: World?, pos: BlockPos?, stack: ItemStack?, player: EntityPlayer?): Boolean {
    return false
  }

  override fun onLeftClickEntity(stack: ItemStack?, player: EntityPlayer?, entity: Entity?): Boolean {
    return true
  }

  override fun onEntitySwing(entityLiving: EntityLivingBase?, stack: ItemStack?): Boolean {
    return true
  }

  override fun getMaxItemUseDuration(stack: ItemStack?): Int = 1000

  override fun getItemUseAction(stack: ItemStack?): EnumAction = EnumAction.NONE

  override fun onItemRightClick(worldIn: World?, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
    playerIn.activeHand = handIn
    startUse()
    return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn))
  }

  override fun onPlayerStoppedUsing(stack: ItemStack?, worldIn: World?, entityLiving: EntityLivingBase?, timeLeft: Int) {
    stopUse()
    super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft)
  }

  fun startUse() {
    println("start use ghook")
  }

  fun stopUse() {
    println("stop use ghook")
  }
}