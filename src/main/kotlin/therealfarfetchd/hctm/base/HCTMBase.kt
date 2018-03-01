package therealfarfetchd.hctm.base

import net.minecraftforge.fml.common.Mod
import therealfarfetchd.hctm.base.utils.HCTMCompatibleVersions
import therealfarfetchd.hctm.base.utils.KotlinLangAdapter

const val ModID = "hctm-base"

@Mod(modid = ModID, modLanguageAdapter = KotlinLangAdapter, acceptedMinecraftVersions = HCTMCompatibleVersions)
object HCTMBase