package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modules.ParasiteModule;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.items.BagItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ParasiteMode extends BaseMode {

    public static final String NAME = "Parasite";

    public ParasiteMode() { super(NAME, ParasiteModule.NAME); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        int bag = DimBag.getBagAccess(player, 0, true, false, false, false);
        if (bag > 0) {
            BagItem.unequipBags(player, bag, player.level(), player.blockPosition(), true).forEach(e -> BagItem.equipBag(target, (BagEntity)e));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            List<Entity> passengers = vehicle.getPassengers();
            if (passengers.size() > 1 && (passengers.get(0) instanceof BagEntity || passengers.get(1) instanceof BagEntity)) {
                Entity pushBack = passengers.get(0);
                pushBack.stopRiding();
                pushBack.boardingCooldown = 0; //there is a cooldown on most entities, disabling it is better than having bags that drop in the ocean while riding a boat :)
                pushBack.startRiding(vehicle);
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
            int bag = DimBag.getBagAccess(player, 0, true, false, false, false);
            if (bag > 0) {
                BagEntity test = BagEntity.create(level, player.blockPosition(), bag);
                if (test.startRiding(vehicle)) {
                    BagItem.unequipBags(player, bag, level, player.blockPosition(), true).forEach(e -> e.remove(Entity.RemovalReason.KILLED));
                    return InteractionResultHolder.success(player.getItemInHand(hand));
                } else
                    test.remove(Entity.RemovalReason.KILLED);
            }
        }
        return super.use(level, player, hand);
    }
}
