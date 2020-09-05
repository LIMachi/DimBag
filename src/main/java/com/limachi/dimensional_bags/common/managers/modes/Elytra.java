package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class Elytra extends Mode {
    public Elytra() { super("Elytra", false, false); }

    @Override
    public ActionResultType onItemUse(EyeData data, World world, PlayerEntity player, int slot, BlockRayTraceResult ray) { return ActionResultType.SUCCESS; }

    @Override
    public ActionResultType onActivateItem(EyeData data, ItemStack stack, PlayerEntity player) {
        if (!player.isElytraFlying()) return ActionResultType.SUCCESS; //only work while the player is flying
        IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(player, 2, FireworkRocketItem.class, (x)->{
            CompoundNBT nbt = x.getChildTag("Fireworks");
            return nbt == null || nbt.getList("Explosions", 10).size() == 0;
        });
        if (res == null)
            res = IDimBagCommonItem.searchItem(player, 2, FireworkRocketItem.class, (x)->true);
        if (res != null) {
            player.world.addEntity(new FireworkRocketEntity(player.world, res.stack, player));
            res.stack.shrink(1);
            res.setStackDirty();
        }
        return ActionResultType.SUCCESS;
    }
}
