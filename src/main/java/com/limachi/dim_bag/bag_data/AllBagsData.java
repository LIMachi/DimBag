package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.World;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
public class AllBagsData implements INBTSerializable<ListTag> {
    public static final Capability<AllBagsData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final AllBagsData INVALID = new AllBagsData();

    private ListTag data = new ListTag();

    public static AllBagsData getInstance() {
        if (World.getLevel(Level.OVERWORLD) instanceof ServerLevel level)
            return level.getCapability(CAPABILITY).orElse(INVALID);
        return INVALID;
    }

    public static int newBagId() {
        AllBagsData b = getInstance();
        if (b != INVALID) {
            b.data.add(new CompoundTag());
            int id = b.data.size();
            RoomData room = new RoomData(id);
            room.initRoomData();
            room.build();
            new ModesData(id).installInitialModes();
            return id;
        }
        return 0;
    }

    public static int maxBagId() {
        AllBagsData b = getInstance();
        if (b != INVALID)
            return b.data.size();
        return -1;
    }

    //only accessible via an instance of class in bag_data package
    protected static CompoundTag getRawBag(int id) {
        AllBagsData b = getInstance();
        if (b != INVALID)
            return b.data.getCompound(id - 1);
        return new CompoundTag();
    }

    @Override
    public ListTag serializeNBT() { return data; }

    @Override
    public void deserializeNBT(ListTag nbt) { data = nbt; }

    @Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
    static class CapabilityHandle implements ICapabilityProvider, INBTSerializable<ListTag> {

        private static final ResourceLocation NAME = new ResourceLocation(DimBag.MOD_ID, "all_bags");

        @SubscribeEvent
        public static void attachCapability(AttachCapabilitiesEvent<Level> event) {
            if (!event.getObject().isClientSide && event.getObject().dimension().equals(Level.OVERWORLD) && !event.getObject().getCapability(CAPABILITY).isPresent())
                event.addCapability(NAME, new CapabilityHandle());
        }

        private AllBagsData do_not_touch_directly = null;
        private final LazyOptional<AllBagsData> LAZY_PROVIDER = LazyOptional.of(this::instance);

        private AllBagsData instance() {
            if (do_not_touch_directly == null)
                do_not_touch_directly = new AllBagsData();
            return do_not_touch_directly;
        }

        @Override
        public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, LAZY_PROVIDER);
        }

        @Override
        public ListTag serializeNBT() { return instance().serializeNBT(); }

        @Override
        public void deserializeNBT(ListTag nbt) { instance().deserializeNBT(nbt); }
    }
}*/