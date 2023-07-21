package com.limachi.dim_bag.capabilities.entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.capabilities.ICopyCapOnDeath;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.annotations.RegisterCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagMode implements INBTSerializable<CompoundTag>, ICopyCapOnDeath<BagMode> {
    @RegisterCapability(targets = {Player.class})
    public static final CapabilityToken<BagMode> TOKEN = new CapabilityToken<>(){};

    @RegisterMsg
    public record SyncBagMode(int bag, String mode) implements IRecordMsg {
        public void clientWork(Player player) { Cap.run(player, TOKEN, c->c.setMode(player, bag, mode)); }
    }

    @RegisterMsg
    public record SyncBagModeOnLogin(CompoundTag modes) implements IRecordMsg {
        public void clientWork(Player player) { Cap.run(player, TOKEN, c->c.deserializeNBT(modes)); }
    }

    @SubscribeEvent
    public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Cap.run(event.getEntity(), TOKEN, c->NetworkManager.toClient(DimBag.MOD_ID, (ServerPlayer)event.getEntity(), new SyncBagModeOnLogin(c.serializeNBT())));
    }

    @SubscribeEvent
    public static void onChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            Cap.run(player, TOKEN, c->{
                for (Map.Entry<Integer, String> m : c.modes.entrySet())
                    NetworkManager.toClient(DimBag.MOD_ID, player, new SyncBagMode(m.getKey(), m.getValue()));
            });
    }

    protected final HashMap<Integer, String> modes = new HashMap<>();

    public String getMode(int bag) { return modes.getOrDefault(bag, "Default"); }

    public void setMode(Player player, int bag, String mode) {
        modes.put(bag, mode);
        if (player instanceof ServerPlayer p)
            NetworkManager.toClient(DimBag.MOD_ID, p, new SyncBagMode(bag, mode));
    }

    @Override
    public void copy(BagMode other) {
        modes.clear();
        modes.putAll(other.modes);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag modes = new ListTag();
        for (Map.Entry<Integer, String> e : this.modes.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("bag", e.getKey());
            entry.putString("mode", e.getValue());
            modes.add(entry);
        }
        CompoundTag out = new CompoundTag();
        out.put("bag_modes", modes);
        return out;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        modes.clear();
        for (Tag e : nbt.getList("bag_modes", Tag.TAG_COMPOUND))
            if (e instanceof CompoundTag entry)
                modes.put(entry.getInt("bag"), entry.getString("mode"));
    }
}