package therealfarfetchd.rswires.common.block

import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import therealfarfetchd.quacklib.common.api.extensions.get
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.item.ItemBlockDelegated
import therealfarfetchd.quacklib.common.api.util.BlockClassLayout
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.rswires.ModID
import java.util.*

abstract class LampBase(val isOn: Boolean, val isInv: Boolean) : Block(Material.ROCK) {
  init {
    setHardness(0.25f)
    soundType = SoundType.STONE
  }

  override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
    if (!worldIn.isRemote) {
      if (this.isOn && !worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOffState(state), 2)
      } else if (!this.isOn && worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOnState(state), 2)
      }
    }
  }

  override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
    if (!worldIn.isRemote) {
      if (this.isOn && !worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOffState(state), 2)
      } else if (!this.isOn && worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOnState(state), 2)
      }
    }
  }

  override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
    if (!worldIn.isRemote) {
      if (this.isOn && !worldIn.isBlockPowered(pos)) {
        worldIn.setBlockState(pos, getOffState(state), 2)
      }
    }
  }

  fun getOnState(state: IBlockState) = getBaseState(true).withProperty(LampProperties.PropColor, state[LampProperties.PropColor])

  fun getOffState(state: IBlockState) = getBaseState(false).withProperty(LampProperties.PropColor, state[LampProperties.PropColor])

  fun getBaseState(on: Boolean) = when {
    isInv && !on -> LampInv.defaultState
    isInv && on  -> LampInvOn.defaultState
    !on          -> Lamp.defaultState
    on           -> LampOn.defaultState
    else         -> error("what?")
  }

  override fun createBlockState() = BlockStateContainer(this, LampProperties.PropColor)

  override fun getStateForPlacement(world: World?, pos: BlockPos?, facing: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand) =
    defaultState.withProperty(LampProperties.PropColor, EnumDyeColor.byMetadata(placer.getHeldItem(hand).metadata))

  override fun getStateFromMeta(meta: Int) = defaultState
    .withProperty(LampProperties.PropColor, EnumDyeColor.byMetadata(meta))

  override fun getMetaFromState(state: IBlockState) = state[LampProperties.PropColor].metadata

  override fun canConnectRedstone(state: IBlockState?, world: IBlockAccess?, pos: BlockPos?, side: EnumFacing?) = true

  override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int) = if (isInv) LampInv.Item else Lamp.Item

  override fun damageDropped(state: IBlockState) = state[LampProperties.PropColor].metadata

  override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState) = getSilkTouchDrop(state)

  override fun getSilkTouchDrop(state: IBlockState) = (if (isInv) LampInv else Lamp).makeStack(meta = damageDropped(state))
}

@BlockDef(
  registerModels = false,
  creativeTab = ModID,
  dependencies = "lumar",
  layout = BlockClassLayout.StaticBlock,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
)
object Lamp : LampBase(false, false) {
  init {
    registryName = ResourceLocation(ModID, "lamp_off")
  }

  val Item = ItemBlockDelegated(this)

  init {
    Item.run {
      hasSubtypes = true
      maxDamage = 0
    }
  }
}

@BlockDef(
  registerModels = false,
  dependencies = "lumar",
  layout = BlockClassLayout.StaticBlock,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
)
object LampOn : LampBase(true, false) {
  init {
    registryName = ResourceLocation(ModID, "lamp_on")
    setLightLevel(1.0f)
  }

  override fun canRenderInLayer(state: IBlockState?, layer: BlockRenderLayer?): Boolean =
    layer in setOf(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT)
}

@BlockDef(
  registerModels = false,
  creativeTab = ModID,
  dependencies = "lumar",
  layout = BlockClassLayout.StaticBlock,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
)
object LampInv : LampBase(false, true) {
  init {
    registryName = ResourceLocation(ModID, "lamp_inv_off")
    setLightLevel(1.0f)
  }

  override fun canRenderInLayer(state: IBlockState?, layer: BlockRenderLayer?): Boolean =
    layer in setOf(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT)

  val Item = ItemBlockDelegated(this)

  init {
    Item.run {
      hasSubtypes = true
      maxDamage = 0
    }
  }
}

@BlockDef(
  registerModels = false,
  dependencies = "lumar",
  layout = BlockClassLayout.StaticBlock,
  metaModels = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
)
object LampInvOn : LampBase(true, true) {
  init {
    registryName = ResourceLocation(ModID, "lamp_inv_on")
  }
}

object LampProperties {
  val PropColor = PropertyEnum.create("color", EnumDyeColor::class.java)
}