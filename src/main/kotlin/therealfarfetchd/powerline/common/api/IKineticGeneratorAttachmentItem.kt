package therealfarfetchd.powerline.common.api

interface IKineticGeneratorAttachmentItem<out T: IKineticGeneratorAttachment> {
  fun create(): T
}