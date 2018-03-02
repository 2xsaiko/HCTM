package therealfarfetchd.retrocomputers

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.LogManager
import therealfarfetchd.hctm.base.utils.HCTMCompatibleVersions
import therealfarfetchd.hctm.base.utils.KotlinLangAdapter
import therealfarfetchd.retrocomputers.common.Proxy
import therealfarfetchd.retrocomputers.common.item.ReadonlyDisk

const val ModID = "retrocomputers"
const val ClientProxy = "therealfarfetchd.$ModID.client.Proxy"
const val ServerProxy = "therealfarfetchd.$ModID.common.Proxy"

@Mod(modid = ModID, acceptedMinecraftVersions = HCTMCompatibleVersions, modLanguageAdapter = KotlinLangAdapter)
object RetroComputers {
  val Logger = LogManager.getLogger(ModID)!!
  val Net = NetworkRegistry.INSTANCE.newSimpleChannel(ModID)!!

  val disks = ReadonlyDisk(
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/minforth.bin") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/forth.bin") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/extforth.bin") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/decompiler.img") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/radio.img") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/sortron.img") },
    { javaClass.classLoader.getResourceAsStream("assets/$ModID/retinal.img") }
  ).apply {
    registryName = ResourceLocation(ModID, "disk")
    unlocalizedName = registryName.toString()
  }

  @SidedProxy(clientSide = ClientProxy, serverSide = ServerProxy)
  lateinit var proxy: Proxy

  @Mod.EventHandler
  fun preInit(e: FMLPreInitializationEvent) = proxy.preInit(e)

  @Mod.EventHandler
  fun init(e: FMLInitializationEvent) = proxy.init(e)

  @Mod.EventHandler
  fun postInit(e: FMLPostInitializationEvent) = proxy.postInit(e)
}