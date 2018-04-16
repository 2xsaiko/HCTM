package therealfarfetchd.retrocomputers.common.api.component

import net.minecraft.util.ResourceLocation
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object ComponentRegistry {
  private var classToType: Map<KClass<out Component>, ResourceLocation> = emptyMap()
  private var typeToConstructor: Map<ResourceLocation, () -> Component> = emptyMap()

  fun <T : Component> register(type: ResourceLocation, clazz: KClass<T>) {
    if (clazz in classToType.keys) error("Component $type already registered!")
    classToType += clazz to type
    val c = clazz.primaryConstructor!!
    c.call()
    typeToConstructor += type to { c.call() }
  }

  fun <T : Component> register(type: ResourceLocation, clazz: Class<T>) {
    register(type, clazz.kotlin)
  }

  fun getType(component: Component) = classToType[component::class]

  fun create(type: ResourceLocation) = typeToConstructor[type]?.invoke()
}