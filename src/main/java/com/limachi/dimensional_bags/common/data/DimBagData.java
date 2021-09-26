package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.OwnerData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class DimBagData extends WorldSavedData { //server side only, client side only has acces to copies of inventories or other data sync through packets (vanilla, forge or modded ones)
    private int lastId = 0;
    public final Chunkloadder chunkloadder = new Chunkloadder();

    DimBagData() { super(MOD_ID); }

    static public DimBagData get() {
        World w = WorldUtils.getOverWorld();
        return w instanceof ServerWorld ? ((ServerWorld)w).getDataStorage().computeIfAbsent(DimBagData::new, MOD_ID) : null;
    }

    public static int getLastId() {
        DimBagData d = get();
        return d == null ? 0 : d.lastId;
    }

    public int newEye(ServerPlayerEntity player, ItemStack bag) {
        int id = ++lastId;
        SubRoomsManager roomsManager = SubRoomsManager.getInstance(id);
        if (roomsManager == null) {
            --lastId;
            return 0;
        }
        World world = WorldUtils.getRiftWorld();
        if (world instanceof ServerWorld) {
            BlockPos eyePos = SubRoomsManager.getEyePos(id);
            WorldUtils.buildRoom(world, eyePos, SubRoomsManager.DEFAULT_RADIUS);
            roomsManager.bagChunkLoading(true);
        }
        WorldSavedDataManager.populateAllById(id);
        SettingsData.getInstance(id).initDefaultSettings();
        if (player.hasPermissions(2) || player.getName().getString().equals("Dev") || player.getName().getString().equals("LIMachi_"))
            ModeManager.execute(id, mm->mm.installMode("Debug")); //if op/dev or LIMachi_, install the Debug mode by default
        ModeManager.execute(id, mm->mm.selectMode("Manual"));
        OwnerData.execute(id, od->od.setPlayer(player));
        if (bag.getTag() == null)
            bag.setTag(new CompoundNBT());
        bag.getTag().putInt(IEyeIdHolder.EYE_ID_KEY, id);
        setDirty();
        return id;
    }

    @Override
    public void load(CompoundNBT compound) {
        DimBag.LOGGER.info("Loadding global data");
        this.lastId = compound.getInt("lastId");
        this.chunkloadder.read(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        DimBag.LOGGER.info("Updating global data");
        compound.putInt("lastId", lastId);
        this.chunkloadder.write(compound);
        return compound;
    }
}
