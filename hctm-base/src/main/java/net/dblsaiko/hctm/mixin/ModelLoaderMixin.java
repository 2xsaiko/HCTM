package net.dblsaiko.hctm.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import com.mojang.datafixers.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import kotlin.jvm.functions.Function2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dblsaiko.hctm.client.render.model.ModelWrapperHandler;
import net.dblsaiko.hctm.ext.ModelDefinitionExt;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Shadow @Final private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow @Final private ResourceManager resourceManager;

    private Function2<BlockState, UnbakedModel, UnbakedModel> modelWrapper;

    private Function2<BlockState, UnbakedModel, UnbakedModel> getModelWrapper() {
        if (modelWrapper == null) {
            modelWrapper = ModelWrapperHandler.INSTANCE.prepare(resourceManager);
        }
        return modelWrapper;
    }

    @Inject(
        method = "loadModel(Lnet/minecraft/util/Identifier;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;putAll(Ljava/util/Map;)V", shift = Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void loadModel(Identifier id, CallbackInfo ci, Identifier identifier2, StateManager stateManager,
                           List list, ImmutableList immutableList, Map map, Map map2, Identifier identifier3,
                           UnbakedModel unbakedModel, @Coerce Object modelDefinition, Pair pair, List list3, Iterator var14,
                           Pair pair2, ModelVariantMap modelVariantMap, Map<BlockState, Pair<UnbakedModel, Supplier<ModelDefinitionExt>>> map4) {
        map4.keySet().forEach(k -> map4.computeIfPresent(k, (state, entry) -> {
            UnbakedModel model = getModelWrapper().invoke(state, entry.getFirst());
            return Pair.of(model, entry.getSecond());
        }));
    }
}
