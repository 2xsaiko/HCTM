package therealfarfetchd.retrocomputers.common.ether

/**
 * Created by marco on 24.06.17.
 */
object RadioEther {
  private var entries: Set<IEtherEntry> = emptySet()

  fun addEntry(entry: IEtherEntry) {
    entries += entry
  }

  fun broadcast(source: EtherSource, data: List<Byte>) {
    cleanupEntries()
    entries
        .filter { it.id != source.id }
        .forEach { it.receiveData(source, data) }
  }

  fun send(source: EtherSource, data: List<Byte>, targetId: Short) {
    cleanupEntries()
    entries
      .filter { it.id == targetId }
        .forEach { it.receiveData(source, data) }
  }

  private fun cleanupEntries() {
    entries = entries.filterNot { it.isInvalid }.toSet()
  }
}