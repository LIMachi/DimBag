package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modules.ParadoxModule;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.saveData.SaveSync;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/*
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
@RegisterSaveData
public class HolderPositions extends AbstractSyncSaveData {

    record Holder(BlockPos pos, String dim, UUID id) {
        public CompoundTag asCompound(int bagId) {
            CompoundTag out = new CompoundTag();
            out.putInt("Bag", bagId);
            out.putLong("Pos", pos.asLong());
            out.putString("Dim", dim);
            out.putUUID("ID", id);
            return out;
        }

        public void temporaryLoad() {
            ((ServerLevel) World.getLevel(dim)).getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 3, pos, true);
        }
    }

    protected final HashMap<Integer, Holder> holders = new HashMap<>();
    protected final HashMap<Integer, Holder> paradoxHolders = new HashMap<>();

    public HolderPositions(String name) { super(name, SaveSync.SERVER_ONLY); }

    public static void setHolder(int bagId, Entity entity) {
        boolean paradox = (entity.level().dimension().equals(DimBag.BAG_DIM) && BagRoom.getRoomId(entity.blockPosition()) == bagId);
        if (!paradox || ParadoxModule.isParadoxCompatible(bagId)) {
            HolderPositions instance = SaveDataManager.getInstance("holder_positions");
            Holder h;
            if (!paradox) {
                instance.paradoxHolders.remove(bagId);
                h = instance.holders.get(bagId);
            }
            else
                h = instance.paradoxHolders.get(bagId);
            if (h != null && h.id == entity.getUUID() && h.pos == entity.blockPosition() && h.dim.equals(entity.level().dimension().location().toString())) return;
            h = new Holder(entity.blockPosition(), entity.level().dimension().location().toString(), entity.getUUID());
            if (!paradox)
                instance.holders.put(bagId, h);
            else
                instance.paradoxHolders.put(bagId, h);
            instance.setDirty();
        }
        else if (entity instanceof BagItemEntity || entity instanceof BagEntity) {
            BagRoom room = BagRoom.getRoom(bagId);
            if (room != null)
                room.leave(entity);
        }
    }

    public static Entity getHolder(int bagId, boolean nonParadoxOnly) {
        if (Sides.isLogicalClient()) return null;
        HolderPositions instance = SaveDataManager.getInstance("holder_positions");
        Holder holder = null;
        if (!nonParadoxOnly)
            holder = instance.paradoxHolders.get(bagId);
        if (holder == null)
            holder = instance.holders.get(bagId);
        if (holder == null)
            return null;
        Holder foundHolder = holder;
        List<Entity> tl = World.getLevel(holder.dim).getEntities((Entity)null, new AABB(holder.pos.offset(-1, -1, -1), holder.pos.offset(1, 1, 1)), e->e.getUUID().equals(foundHolder.id));
        return tl.size() == 1 ? tl.get(0) : null;
    }

    public static Pair<ResourceKey<Level>, BlockPos> getHolderLastKnownPosition(int bagId, boolean nonParadoxOnly) {
        if (Sides.isLogicalClient()) return null;
        HolderPositions instance = SaveDataManager.getInstance("holder_positions");
        Holder holder = null;
        if (!nonParadoxOnly)
            holder = instance.paradoxHolders.get(bagId);
        if (holder == null)
            holder = instance.holders.get(bagId);
        if (holder == null)
            return null;
        return new Pair<>(World.getLevel(holder.dim()).dimension(), holder.pos);
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        holders.forEach((i, h)->list.add(h.asCompound(i)));
        compoundTag.put("Holders", list);
        ListTag list2 = new ListTag();
        paradoxHolders.forEach((i, h)->list2.add(h.asCompound(i)));
        compoundTag.put("ParadoxHolders", list2);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        holders.clear();
        compoundTag.getList("Holders", Tag.TAG_COMPOUND).forEach(t->{
            CompoundTag c = (CompoundTag)t;
            holders.put(c.getInt("Bag"), new Holder(BlockPos.of(c.getLong("Pos")), c.getString("Dim"), c.getUUID("ID")));
        });
        paradoxHolders.clear();
        compoundTag.getList("ParadoxHolders", Tag.TAG_COMPOUND).forEach(t->{
            CompoundTag c = (CompoundTag)t;
            paradoxHolders.put(c.getInt("Bag"), new Holder(BlockPos.of(c.getLong("Pos")), c.getString("Dim"), c.getUUID("ID")));
        });
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        HolderPositions instance = SaveDataManager.getInstance("holder_positions");
        instance.holders.values().forEach(Holder::temporaryLoad);
    }
}
*/