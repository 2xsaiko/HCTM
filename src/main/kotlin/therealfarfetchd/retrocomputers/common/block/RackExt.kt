package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import therealfarfetchd.quacklib.common.api.extensions.minus
import therealfarfetchd.quacklib.common.api.extensions.plus
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.util.checkIntegrity
import therealfarfetchd.retrocomputers.common.util.getRackPos
import java.util.*
import net.minecraft.block.Block as MCBlock
import net.minecraft.tileentity.TileEntity as MCTile

object RackExt {
  val Type = ResourceLocation(ModID, "rack_ext")

  class Tile : MCTile() {
    var offset: BlockPos = BlockPos.ORIGIN
      set(value) {
        field = value
        markDirty()
      }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
      super.writeToNBT(compound)
      val nbt = QNBTCompound(compound)
      nbt.int["oX"] = offset.x
      nbt.int["oY"] = offset.y
      nbt.int["oZ"] = offset.z
      return compound
    }

    override fun readFromNBT(compound: NBTTagCompound) {
      super.readFromNBT(compound)
      val nbt = QNBTCompound(compound)
      offset = BlockPos(nbt.int["oX"], nbt.int["oY"], nbt.int["oZ"])
    }

    override fun getUpdateTag() = writeToNBT(NBTTagCompound())

    override fun handleUpdateTag(tag: NBTTagCompound) {
      readFromNBT(tag)
    }
  }

  @Suppress("OverridingDeprecatedMember")
  object Block : MCBlock(Material.IRON), ITileEntityProvider {
    init {
      registryName = Type
      unlocalizedName = Rack.Type.toString()
      setHarvestLevel("pickaxe", 2)
      setHardness(1.0f)
    }

    override fun neighborChanged(state: IBlockState?, world: World, pos: BlockPos, blockIn: MCBlock?, fromPos: BlockPos?) {
      if (!checkIntegrity(world, pos)) {
        world.setBlockToAir(pos)
      }
    }

    override fun getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack {
      val rackPos = getRackPos(world, pos) ?: return ItemStack.EMPTY
      val rackState = world.getBlockState(rackPos)
      @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
      return rackState.block.getPickBlock(rackState, null, world, rackPos, player)
    }

    @Suppress("DEPRECATION")
    override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB {
      val rackPos = getRackPos(world, pos) ?: return super.getSelectedBoundingBox(state, world, pos)
      val rackState = world.getBlockState(rackPos)
      if (rackState.block != Rack.Block) return super.getSelectedBoundingBox(state, world, pos)
      return Rack.Block.getSelectedBoundingBox(rackState, world, rackPos)
    }

    @Suppress("DEPRECATION")
    override fun getBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB {
      val rackPos = getRackPos(world, pos) ?: return super.getBoundingBox(state, world, pos)
      val rackState = world.getBlockState(rackPos)
      if (rackState.block != Rack.Block) return super.getBoundingBox(state, world, pos)
      return (Rack.Block.getCompleteBoundingBox(rackState, world, rackPos) + rackPos - pos).intersect(FULL_BLOCK_AABB)
    }

    override fun collisionRayTrace(blockState: IBlockState, worldIn: World, pos: BlockPos, start: Vec3d, end: Vec3d): RayTraceResult? {
      return Rack.Block.collisionRayTrace(blockState, worldIn, pos, start, end)
    }

    override fun createNewTileEntity(worldIn: World?, meta: Int) = Tile()

    override fun getRenderType(state: IBlockState?) = EnumBlockRenderType.INVISIBLE

    override fun isOpaqueCube(state: IBlockState?) = false

    override fun isFullCube(state: IBlockState?) = false

    override fun getItemDropped(state: IBlockState?, rand: Random?, fortune: Int) = Items.AIR
  }
}