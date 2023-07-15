package com.limachi.dim_bag.capabilities.levels;

import com.limachi.dim_bag.DimBag;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagsDataProvider implements ICapabilityProvider, INBTSerializable<ListTag> {

    private static final ResourceLocation NAME = new ResourceLocation(DimBag.MOD_ID, "all_bags");

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Level> event) {
        if (!event.getObject().isClientSide && event.getObject().dimension().equals(Level.OVERWORLD) && !event.getObject().getCapability(IBagsData.CAPABILITY).isPresent()) {
            BagsDataProvider handler = new BagsDataProvider();
            event.addCapability(NAME, handler);
            event.addListener(handler::invalidateCaps);
        }
    }

    private BagsData do_not_touch_directly = null;
    private final LazyOptional<BagsData> LAZY_PROVIDER = LazyOptional.of(this::instance);

    private BagsData instance() {
        if (do_not_touch_directly == null)
            do_not_touch_directly = new BagsData();
        return do_not_touch_directly;
    }

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return IBagsData.CAPABILITY.orEmpty(cap, LAZY_PROVIDER.cast());
    }

    @Override
    public ListTag serializeNBT() { return instance().serializeNBT(); }

    @Override
    public void deserializeNBT(ListTag nbt) { instance().deserializeNBT(nbt); }

    public void invalidateCaps() {
        do_not_touch_directly.invalidate();
        do_not_touch_directly = null;
    }
}
*/