package com.example.aeropipefuel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;

/**
 * «Жидкостный вход» двигателя. Жидкость копится в маленьком буфере; когда набирается порция
 * (по data map — например 1000 mB лавы), в слот топлива двигателя кладётся соответствующий предмет
 * (лавовое ведро). Если слот занят — буфер заполняется и труба перестаёт подавать жидкость,
 * пока двигатель не выгорит.
 */
public final class EngineFluidHandler implements IFluidHandler {
    private static final int TANK_CAPACITY = 1000;

    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final BlockEntity be;

    public EngineFluidHandler(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.be = be;
    }

    private FluidBuffer buffer() {
        return be.getData(AeroPipeFuel.BUFFER.get());
    }

    /** Предметный инвентарь двигателя: сначала через capability, затем (запасной путь) через поле блока. */
    private IItemHandler itemHandler() {
        IItemHandler h = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, null);
        if (h != null) return h;
        for (Direction d : Direction.values()) {
            h = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, d);
            if (h != null) return h;
        }
        for (Class<?> c = be.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (IItemHandler.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object o = f.get(be);
                        if (o instanceof IItemHandler ih) return ih;
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        // пробуем следующее поле
                    }
                }
            }
        }
        return null;
    }

    /** Переводит накопленную жидкость в топливный предмет, если двигатель готов его принять. */
    private void flush(FluidBuffer buf) {
        if (buf.stack.isEmpty()) return;
        FluidFuel fuel = FluidFuel.of(buf.stack.getFluid());
        if (fuel == null || buf.stack.getAmount() < fuel.amount()) return;
        IItemHandler items = itemHandler();
        if (items == null) return;

        ItemStack fuelStack = new ItemStack(fuel.item());
        if (!ItemHandlerHelper.insertItem(items, fuelStack.copy(), true).isEmpty()) return;
        if (!ItemHandlerHelper.insertItem(items, fuelStack.copy(), false).isEmpty()) return;

        buf.stack.shrink(fuel.amount());
        if (buf.stack.isEmpty()) buf.stack = FluidStack.EMPTY;
        be.setChanged();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return level.isClientSide ? FluidStack.EMPTY : buffer().stack.copy();
    }

    @Override
    public int getTankCapacity(int tank) {
        return TANK_CAPACITY;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && FluidFuel.of(stack.getFluid()) != null;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (level.isClientSide || resource.isEmpty()) return 0;
        FluidFuel fuel = FluidFuel.of(resource.getFluid());
        if (fuel == null) return 0;

        FluidBuffer buf = buffer();
        flush(buf); // вдруг двигатель уже выгорел и слот освободился
        if (!buf.stack.isEmpty() && !FluidStack.isSameFluidSameComponents(buf.stack, resource)) return 0;

        int filled = Math.min(fuel.amount() - buf.stack.getAmount(), resource.getAmount());
        if (filled <= 0) return 0;

        if (action.execute()) {
            if (buf.stack.isEmpty()) buf.stack = resource.copyWithAmount(filled);
            else buf.stack.grow(filled);
            be.setChanged();
            flush(buf);
        }
        return filled;
    }

    /** Жидкость обратно не достаётся. */
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
