package therealfarfetchd.rswires.common.init

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.BlockEntityType.Builder
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.hctm.common.block.BaseWireBlockEntity
import therealfarfetchd.hctm.common.util.delegatedNotNull
import therealfarfetchd.rswires.ModID
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty

object BlockEntityTypes {

  private val tasks = mutableListOf<() -> Unit>()

  val RedAlloyWire by create("red_alloy_wire", ::BaseWireBlockEntity)
  val InsulatedWire by create("insulated_wire", ::BaseWireBlockEntity)
  val BundledCable by create("bundled_cable", ::BaseWireBlockEntity)

  private fun <T : BlockEntity> create(name: String, builder: () -> T, vararg blocks: Block): ReadOnlyProperty<BlockEntityTypes, BlockEntityType<T>> {
    var regType: BlockEntityType<T>? = null
    tasks += { regType = Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), Builder.create(Supplier(builder), *blocks).build(null)) }
    return delegatedNotNull { regType }
  }

  private fun <T : BlockEntity> create(name: String, builder: (BlockEntityType<T>) -> T): ReadOnlyProperty<BlockEntityTypes, BlockEntityType<T>> {
    var regType: BlockEntityType<T>? = null
    tasks += {
      var type: BlockEntityType<T>? = null
      val s = Supplier { builder(type!!) }
      type = Builder.create(s).build(null)
      regType = Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), type)
    }
    return delegatedNotNull { regType }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}