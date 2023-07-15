package com.limachi.dim_bag.capabilities.levels;

import com.limachi.dim_bag.bag_data.SlotData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.World;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

/*
public interface IBagsData extends INBTSerializable<ListTag> {
    Capability<IBagsData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @Configs.Config(path = "rooms", cmt = "Initial size of a new bag (in blocks, including walls)")
    int DEFAULT_ROOM_RADIUS = 3;

    @Configs.Config(path = "rooms", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    int ROOM_SPACING = 1024;

    @Configs.Config(path = "rooms", min = "3", max = "126", cmt = "Maximum size of a bag (in blocks, including walls)")
    int MAXIMUM_ROOM_RADIUS = 64;

    static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    static LazyOptional<IBagsData> getInstance() {
        if (World.getLevel(Level.OVERWORLD) instanceof ServerLevel level)
            return level.getCapability(CAPABILITY);
        Log.error("accessed client side!");
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < 10 && i < stack.length; ++i)
            Log.error(stack[i].toString());
        return LazyOptional.empty();
    }

    static int maxId() { return getInstance().map(IBagsData::maxBagId).orElse(0); }

    static LazyOptional<IBagInstance> bag(int id) {
        return getInstance().map(d->d.getBag(id)).orElse(LazyOptional.empty());
    }

    static LazyOptional<IBagInstance> bag(BlockPos pos) {
        LazyOptional<IBagsData> handle = getInstance();
        if (handle.isPresent()) {
            IBagsData instance = handle.resolve().get();
            int id = (pos.getX() / ROOM_SPACING) + 1;
            if (id > 0)
                return instance.getBag(id);
        }
        return LazyOptional.empty();
    }

    static int id(BlockPos pos) { return bag(pos).map(IBagInstance::bagId).orElse(0); }

    static boolean isWall(BlockPos pos) { return bag(pos).map(b->b.isWall(pos)).orElse(false); }

    int newBagId();
    int maxBagId();
    LazyOptional<IBagInstance> getBag(int id);

    interface IBagInstance {
        //common
        int bagId();
        void storeOn(CompoundTag instance);

        //holder
        void setHolder(Entity entity);
        Optional<Entity> getHolder(boolean nonParadoxOnly);
        Optional<Pair<Level, BlockPos>> getHolderPosition(boolean nonParadoxOnly);

        //modes
        boolean installMode(String name);

        //modules
        void simpleInstall(String name, BlockPos pos);
        void simpleUninstall(String name, BlockPos pos);
        boolean isPresent(String name);
        void compoundInstall(String name, BlockPos pos, CompoundTag data);
        CompoundTag compoundUninstall(String name, BlockPos pos);
        CompoundTag getInstalledCompound(String name, BlockPos pos);

        //room
        boolean isWall(BlockPos pos);
        boolean isInRoom(BlockPos pos);
        void temporaryChunkLoad();

        //tp
        Entity enter(Entity entity, boolean proxy);
        Entity leave(Entity entity);

        //slots
        LazyOptional<SlotData> getSlots();
    }
}
*/