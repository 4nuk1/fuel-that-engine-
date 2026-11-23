package com.example.aeropipefuel;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.FluidStack;

/** Содержимое буфера двигателя: накапливает жидкость до размера одной «порции» топлива. */
public final class FluidBuffer {
    public static final Codec<FluidBuffer> CODEC = FluidStack.OPTIONAL_CODEC.xmap(FluidBuffer::new, b -> b.stack);

    public FluidStack stack;

    public FluidBuffer() {
        this(FluidStack.EMPTY);
    }

    public FluidBuffer(FluidStack stack) {
        this.stack = stack;
    }
}
