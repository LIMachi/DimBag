package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class OwnerData extends WorldSavedDataManager.EyeWorldSavedData {

    public static final UUID NULLID = new UUID(0, 0);
    private UUID id = NULLID;
    private WeakReference<PlayerEntity> playerRef = new WeakReference<>(null);
    private String name = "";

    public OwnerData(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
    }

    /**
     * @param player: the player to be stored as owner
     */
    public void setPlayer(PlayerEntity player) {
        playerRef = new WeakReference<>(player);
        if (player != null) {
            name = player.getDisplayName().getString();
            id = player.getUniqueID();
        } else {
            name = "";
            id = NULLID;
        }
        markDirty();
    }

    /**
     * @return if available (connected to the server and valid), return the stored player
     */
    public PlayerEntity getPlayer() {
        if (id.equals(NULLID)) return null;
        PlayerEntity player = playerRef.get();
        if (player == null) {
            MinecraftServer server = DimBag.getServer();
            if (server != null) {
                player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null) {
                    playerRef = new WeakReference<>(player);
                    name = player.getDisplayName().getString();
                    markDirty();
                }
            }
        }
        return player;
    }

    /**
     * @return the name of the owner, the player isn't required to be connected/valid, return "" as an invalid/unset owner
     */
    public String getPlayerName() { return name; }

    /**
     * @return the uuid of the owner, the player isn't required to be connected/valid, return OwnerData.NULLID as an invalid/unset owner (aka new UUID(0,0))
     */
    public UUID getPlayerUUID() { return id; }

    @Override
    public void read(CompoundNBT nbt) {
        id = nbt.getUniqueId("Id");
        name = nbt.getString("Name");
        if (DimBag.isServer(null) && !id.equals(NULLID))
            playerRef = new WeakReference<>(DimBag.getServer().getPlayerList().getPlayerByUUID(id));
        else
            playerRef = new WeakReference<>(null);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putUniqueId("Id", id);
        nbt.putString("Name", name);
        return nbt;
    }

    static public OwnerData getInstance(int id) {
        return WorldSavedDataManager.getInstance(OwnerData.class, null, id);
    }

    static public <T> T execute(int id, Function<OwnerData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(OwnerData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<OwnerData> executable) {
        return WorldSavedDataManager.execute(OwnerData.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}