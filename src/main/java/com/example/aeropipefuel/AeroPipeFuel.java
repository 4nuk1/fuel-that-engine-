package com.example.aeropipefuel;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod(AeroPipeFuel.MOD_ID)
public class AeroPipeFuel {
    public static final String MOD_ID = "aeropipefuel";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Пространство имён и часть пути блоков двигателя из Create Simulated / Aeronautics. */
    private static final String ENGINE_NAMESPACE = "simulated";
    private static final String ENGINE_PATH_PART = "portable_engine";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    /** Небольшой буфер жидкости на блоке двигателя (сохраняется вместе с блоком). */
    public static final Supplier<AttachmentType<FluidBuffer>> BUFFER = ATTACHMENTS.register("fluid_buffer",
            () -> AttachmentType.builder(FluidBuffer::new).serialize(FluidBuffer.CODEC).build());

    public AeroPipeFuel(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        modBus.addListener(this::registerDataMaps);
        modBus.addListener(this::registerCapabilities);
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(FluidFuel.TYPE);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        List<Block> engines = new ArrayList<>();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals(ENGINE_NAMESPACE) && id.getPath().contains(ENGINE_PATH_PART)) {
                engines.add(entry.getValue());
            }
        }
        if (engines.isEmpty()) {
            LOGGER.warn("[{}] Блоки Portable Engine ({}:*{}*) не найдены — Create Aeronautics установлен?",
                    MOD_ID, ENGINE_NAMESPACE, ENGINE_PATH_PART);
            return;
        }
        LOGGER.info("[{}] Жидкостная заправка добавлена для {} блоков двигателя.", MOD_ID, engines.size());
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> be == null ? null : new EngineFluidHandler(level, pos, state, be),
                engines.toArray(new Block[0]));
    }
}
