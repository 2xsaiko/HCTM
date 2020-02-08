package net.dblsaiko.hctm.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.dblsaiko.hctm.ext.ModelDefinitionExt;

@Mixin(targets = "net/minecraft/client/render/model/ModelLoader$ModelDefinition")
public class ModelDefinitionMixin implements ModelDefinitionExt {
}
