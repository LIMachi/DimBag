package com.limachi.dim_bag.modes;

import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
@RegisterSaveData
public class ModeManager extends AbstractSyncSaveData {
    public static final AbstractMode[] MODES = {
            new Default(),
//            new Tank(),
//            new Settings(),
//            new Capture(),
//            new Debug(),
//            new Manual()
    };

    public static int getModeIndex(String name) {
        for (int i = 0; i < MODES.length; ++i)
            if (MODES[i].NAME.equals(name))
                return i;
        return -1;
    }

    public static AbstractMode getMode(String name) {
        int i;
        if ((i = getModeIndex(name)) == -1)
            return MODES[0];
        return MODES[i];
    }

    private CompoundTag installedModesData;

    public ModeManager(String name, SaveSync sync) {
        super(name, sync);
        installedModesData = new CompoundTag();
        for (AbstractMode mode : MODES)
            if (mode.IS_INSTALLED_BY_DEFAULT)
                installedModesData.put(mode.NAME, new CompoundTag());
    }

    public void installMode(String name) {
        if (!installedModesData.contains(name))
            installedModesData.put(name, new CompoundTag());
        setDirty();
    }

    public String getSelectedMode(Entity entity) {
        if (entity == null) return "Default";
        String t = entity.getPersistentData().getCompound("PlayerPersisted").getString("DimBagMode"); //PlayerPersisted is a special compound in player that is kept when the player respawn (copied from old player entity to the new respawned one), while getPersistentData only return a compound that is stored on the disk (FIXME: mode does not seem to be sync client side, resulting in the bag not showing in what mode it is)
        return installedModesData.contains(t) ? t : "Default";
    }

    public Set<String> getInstalledModes() { return installedModesData.getAllKeys(); }

    public String getInstalledMode(int i) { return (String)installedModesData.getAllKeys().toArray()[i]; }

    public void selectMode(Entity entity, int i) {
        if (i < 0 || i >= installedModesData.getAllKeys().size()) return;
        if (!entity.getPersistentData().contains("PlayerPersisted"))
            entity.getPersistentData().put("PlayerPersisted", new CompoundNBT());
        entity.getPersistentData().getCompound("PlayerPersisted").putString("DimBagMode", getInstalledMode(i));
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            NetworkManager.toClient(player, new PlayerPersistentDataAction(PlayerPersistentDataAction.Actions.MERGE, NBTUtils.toCompoundNBT("PlayerPersisted", NBTUtils.toCompoundNBT("DimBagMode", getInstalledMode(i)))));
        }
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public void load(CompoundTag compoundTag) {

    }
}*/
