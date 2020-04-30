package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IdHandler;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class Bag extends Item {

    public class BagEvent extends Event {
        public final boolean leftClick;
        public final boolean croushing;
        public final boolean offHand;
        public final int bagId;
        public final ServerPlayerEntity player;

        BagEvent(boolean leftClick, boolean croushing, boolean offHand, int bagId, ServerPlayerEntity player) {
            this.leftClick = leftClick;
            this.croushing = croushing;
            this.offHand = offHand;
            this.bagId = bagId;
            this.player = player;
        }

        @Override
        public boolean isCancelable() { return true; }
    }

    @Override
    public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) { //detect left click event and convert it to my mod event
        if (entity.world.isRemote() || !(entity instanceof ServerPlayerEntity)) return true; //make sure this event will only be fired server side
        BagEvent event = new BagEvent(true, ((ServerPlayerEntity)entity).isCrouching(), false, new IdHandler(stack).getId(), (ServerPlayerEntity) entity);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) { //detect right click event and convert it to my mod event
        if (world.isRemote() || !(player instanceof ServerPlayerEntity)) return new ActionResult<>(ActionResultType.FAIL, player.getHeldItem(hand)); //make sure this event will only be fired server side
        BagEvent event = new BagEvent(false, player.isCrouching(), hand == Hand.OFF_HAND, new IdHandler(player.getHeldItem(hand)).getId(), (ServerPlayerEntity) player);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return new ActionResult<>(ActionResultType.FAIL, player.getHeldItem(hand));
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) { //FIXME: add logic to monitor player and others
        if (worldIn.isRemote()) return; //do nothing client side
        DimBagData dataHandler = DimBagData.get(entityIn.getServer());
        if (entityIn instanceof ServerPlayerEntity && new IdHandler(stack).getId() == 0) { //detected unset bag, triggering new bag construction
            dataHandler.newEye((PlayerEntity)entityIn).getId().write(stack);
        }
        EyeData data = dataHandler.getEyeData(new IdHandler(stack).getId());
        data.updateBagPosition(entityIn.getPosition(), entityIn.dimension); //set the bag position to the current holding player
    }

    public Bag() {
        super(new Properties().group(DimensionalBagsMod.ItemGroup.instance));
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) { //should use this only if a player or dispenser used the item (will do later)
        Entity ent = new BagEntity(Registries.BAG_ENTITY.get(), world);
        ent.setPosition(location.getPosX(), location.getPosY(), location.getPosZ());
        ent.setInvulnerable(true); //make sure the entity can't be destroyed (except by player events)
        ((BagEntity)ent).enablePersistence(); //make sure the entity can't despawn
        IdHandler id = new IdHandler(stack);
        id.write((BagEntity)ent);
        CompoundNBT item = new CompoundNBT();
        stack.write(item);
        ent.getPersistentData().put("ItemBag", item); //attach all the informations of the bag to the entity
        //if (stack.getDisplayName() != Registries.BAG_ITEM.get().getName()) { //if the bag was renamed, rename  the entity
            ent.setCustomName(stack.getDisplayName());
            ent.setCustomNameVisible(true);
        //}
        return ent;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (world == null) return;
        IdHandler id = new IdHandler(stack);
        if (id.getId() == 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", id.getId()));
    }
}
