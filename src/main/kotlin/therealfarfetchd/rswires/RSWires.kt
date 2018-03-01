package therealfarfetchd.rswires

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import therealfarfetchd.hctm.base.utils.HCTMCompatibleVersions
import therealfarfetchd.hctm.base.utils.KotlinLangAdapter
import therealfarfetchd.rswires.common.Proxy

const val ModID = "rswires"
const val ClientProxy = "therealfarfetchd.$ModID.client.Proxy"
const val ServerProxy = "therealfarfetchd.$ModID.common.Proxy"

@Mod(modid = ModID, modLanguageAdapter = KotlinLangAdapter, acceptedMinecraftVersions = HCTMCompatibleVersions)
object RSWires {
  val Logger = LogManager.getLogger(ModID)!!

  @SidedProxy(clientSide = ClientProxy, serverSide = ServerProxy)
  lateinit var proxy: Proxy

  @Mod.EventHandler
  fun preInit(e: FMLPreInitializationEvent) = proxy.preInit(e)

  @Mod.EventHandler
  fun init(e: FMLInitializationEvent) = proxy.init(e)

  @Mod.EventHandler
  fun postInit(e: FMLPostInitializationEvent) = proxy.postInit(e)
}