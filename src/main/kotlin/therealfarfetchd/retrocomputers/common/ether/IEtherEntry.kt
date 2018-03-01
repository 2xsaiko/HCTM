package therealfarfetchd.retrocomputers.common.ether

/**
 * Created by marco on 24.06.17.
 */
interface IEtherEntry {

  fun receiveData(src: EtherSource, data: List<Byte>)

  val id: Short

  val isInvalid: Boolean

}