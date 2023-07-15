package com.limachi.dim_bag.items;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.integration.Curios.CuriosIntegration;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class VirtualBagItem extends BagItem {

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    @SubscribeEvent
    public static void onKeyPress(KeyMapController.KeyMapChangedEvent event) {
        if (event.getChangedKeys()[DimBag.BAG_KEY.getId()]) {
            if (DimBag.BAG_KEY.getState(event.getPlayer()) && !(event.getPlayer().getMainHandItem().getItem() instanceof BagItem)) {
                int id = DimBag.getBagAccess(event.getPlayer(), 0, true, false, true);
                if (id > 0)
                    event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, create(id, event.getPlayer().getMainHandItem()));
            }
            else if (event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof VirtualBagItem)
                event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, original(event.getPlayer().getMainHandItem()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @StaticInitClient
    public static void prop() {
        ClientRegistries.registerItemModelProperty(R_ITEM, new ResourceLocation(DimBag.MOD_ID, "bag_mode_property"), BagItem::getMode);
    }

    public VirtualBagItem() {}

    public static final String ORIGINAL_STACK_KEY = "original_stack";

    public static ItemStack create(int id, ItemStack original) {
        ItemStack out = new ItemStack(R_ITEM.get());
        CompoundTag tag = out.getOrCreateTag();
        tag.putInt(BAG_ID_KEY, id);
        tag.put(ORIGINAL_STACK_KEY, original.save(new CompoundTag()));
        return out;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) { return null; } //FIXME

    public static ItemStack original(ItemStack virtualBag) {
        if (!(virtualBag.getItem() instanceof VirtualBagItem))
            return virtualBag;
        CompoundTag tag = virtualBag.getTag();
        if (tag == null)
            return ItemStack.EMPTY;
        return ItemStack.of(tag.getCompound(ORIGINAL_STACK_KEY));
    }

    protected static boolean stillValid(Player player) {
        ItemStack stack = player.getMainHandItem();
        int id = BagItem.getBagId(stack);
        return id > 0 && DimBag.BAG_KEY.getState(player) && id == DimBag.getBagAccess(player, id, true, false, true);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) { return true; }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        ItemStack original = original(stack);
        Entity entity = original.getItem().createEntity(level, location, original);
        if (entity == null) {
            entity = new ItemEntity(level, location.position().x, location.position().y, location.position().z, original);
            entity.setDeltaMovement(location.getDeltaMovement());
            ((ItemEntity)entity).setDefaultPickUpDelay();
        }
        return entity;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(stack.getItem() instanceof VirtualBagItem)) return;
        if (entity instanceof ServerPlayer player) {
            if (selected && slot >= 0 && slot <= 3)
                selected = player.getInventory().getItem(slot) == stack; //FIXME: fix, selected might be triggered by offhand/armor due to error in Inventory#tick
            SlotAccess sa = CuriosIntegration.searchItemByExactStack(entity, stack);
            ItemStack o = original(stack);
            boolean valid = stillValid(player);
            sa.set(o);
            o.getItem().inventoryTick(o, level, entity, slot, selected);
            if (!selected || player instanceof FakePlayer || !valid) return;
            ItemStack s = sa.get();
            if (!(s.getItem() instanceof VirtualBagItem) && !s.equals(o, false))
                stack.getOrCreateTag().put(ORIGINAL_STACK_KEY, s.save(new CompoundTag()));
            sa.set(stack);
        }
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) { return false; }

    protected static UseOnContext rebuildContext(UseOnContext original) {
        return new UseOnContext(original.getPlayer(), original.getHand(), new BlockHitResult(original.getClickLocation(), original.getClickedFace(), original.getClickedPos(), original.isInside()));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        if (context.getHand() == InteractionHand.MAIN_HAND && stillValid(player))
            return super.onItemUseFirst(stack, context);
        player.setItemInHand(context.getHand(), original(player.getItemInHand(context.getHand())));
        return super.onItemUseFirst(player.getItemInHand(context.getHand()), rebuildContext(context));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        if (ctx.getHand() == InteractionHand.MAIN_HAND && stillValid(player)) {
            if (KeyMapController.SNEAK.getState(player) && player.level().dimension().equals(DimBag.BAG_DIM)) {
                BagsData.runOnBag(player.level(), player.blockPosition(), b->b.leave(player));
                return InteractionResult.CONSUME;
            }
            return super.useOn(ctx);
        }
        player.setItemInHand(ctx.getHand(), original(player.getItemInHand(ctx.getHand())));
        return super.useOn(rebuildContext(ctx));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(hand));
        if (hand == InteractionHand.MAIN_HAND && stillValid(player)) {
            if (KeyMapController.SNEAK.getState(player) && player.level().dimension().equals(DimBag.BAG_DIM)) {
                BagsData.runOnBag(player.level(), player.blockPosition(), b->b.leave(player));
                return InteractionResultHolder.consume(player.getItemInHand(hand));
            }
            return super.use(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        player.setItemInHand(hand, original(stack));
        InteractionResultHolder<ItemStack> out = stack.use(level, player, hand);
        return (out.getObject().getItem() instanceof BagItem || out.getObject().getItem() instanceof VirtualBagItem) ? new InteractionResultHolder<>(out.getResult(), stack) : out;
    }
}
