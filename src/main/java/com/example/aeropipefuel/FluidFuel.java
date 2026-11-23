package com.example.aeropipefuel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import javax.annotation.Nullable;

/**
 * Data map жидкости: {@code data/<namespace>/data_maps/fluid/engine_fuel.json} (id карты: aeropipefuel:engine_fuel).
 * Правило: {@code amount} mB жидкости превращаются в один предмет {@code item}, который двигатель сжигает
 * как обычное топливо (время горения берётся из печного топлива этого предмета).
 */
public record FluidFuel(Item item, int amount) {
    public static final Codec<FluidFuel> CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(FluidFuel::item),
            Codec.intRange(1, 1_000_000).optionalFieldOf("amount", 1000).forGetter(FluidFuel::amount)
    ).apply(i, FluidFuel::new));

    public static final DataMapType<Fluid, FluidFuel> TYPE = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(AeroPipeFuel.MOD_ID, "engine_fuel"),
            Registries.FLUID, CODEC).build();

    @Nullable
    public static FluidFuel of(Fluid fluid) {
        return BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(TYPE);
    }
}
