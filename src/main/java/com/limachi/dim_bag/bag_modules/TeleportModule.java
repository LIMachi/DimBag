package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.menus.TeleporterMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

@SuppressWarnings({"unused", "deprecation"})
public class TeleportModule extends BaseModule {

    public Component DEFAULT_LABEL = Component.translatable("teleporter.data.label.default");

    @RegisterBlock
    public static RegistryObject<TeleportModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos pos2, boolean bool) {
        BagsData.runOnBag(level, pos, b->b.getInstalledCompound("teleport", pos).putBoolean("active", level.getBestNeighborSignal(pos) == 0));
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag().copy();
        if (data.contains("display", Tag.TAG_COMPOUND)) {
            data.putString("label", data.getCompound("display").getString("Name"));
            data.remove("display");
        }
        if (!data.contains("active", Tag.TAG_BYTE))
            data.putBoolean("active", true);
        if (!data.contains("label", Tag.TAG_STRING))
            data.putString("label", Component.Serializer.toJson(DEFAULT_LABEL));
        bag.compoundInstall("teleport", pos, data);
        if (bag.getModeData("Capture").isEmpty())
            bag.installMode("Capture");
        bag.getModeData("Capture").ifPresent(c->{
            if (c.getBoolean("disabled")) {
                c.putBoolean("disabled", false);
                c.putLong("selected", pos.asLong());
            } else if (!c.contains("selected"))
                c.putLong("selected", pos.asLong());
        });
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.getModeData("Capture").ifPresent(c->{
            ListTag teleporters = bag.unsafeRawAccess().getCompound("modules").getList("teleport", Tag.TAG_COMPOUND);
            if (teleporters.size() <= 1)
                c.putBoolean("disabled", true);
            else if (c.getLong("selected") == pos.asLong()) {
                long p = teleporters.getCompound(0).getLong("position");
                if (p != 0)
                    c.putLong("selected", p);
                else
                    c.remove("selected");
            }
        });
        stack.getOrCreateTag().merge(bag.compoundUninstall("teleport", pos));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TeleporterMenu.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    public static BlockPos getDestination(BagInstance bag, Entity entity) {
        return bag.getModeData("Capture").map(c->{
            BlockPos selected = BlockPos.of(c.getLong("selected"));
            CompoundTag target = bag.getInstalledCompound("teleport", selected);
            if (!target.getBoolean("active"))
                return null;
            if (entity instanceof Player && !target.getBoolean("affect_players"))
                return null;
            String name = entity.getDisplayName().getString();
            boolean whitelist = target.getBoolean("white_list");
            for (Tag t : target.getList("filters", Tag.TAG_STRING))
                if (t instanceof StringTag s && s.getAsString().equals(name))
                    return whitelist ? selected.relative(Direction.UP) : null;
            return whitelist ? null : selected.relative(Direction.UP);
        }).orElse(null);
    }
}
