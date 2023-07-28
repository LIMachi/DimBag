package com.limachi.dim_bag.entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.BaseModule;
import com.limachi.dim_bag.bag_modules.ParasiteModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.PlayerUtils;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.EntityAttributeBuilder;
import com.limachi.lim_lib.registries.annotations.RegisterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BagEntity extends Mob {

    @RegisterEntity(width = 0.4f, height = 0.8f)
    public static RegistryObject<EntityType<BagEntity>> R_TYPE;

    @EntityAttributeBuilder
    public static AttributeSupplier.Builder attributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.);
    }

    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();

    public BagEntity(EntityType<? extends Mob> type, Level lvl) { super(type, lvl); }

    public static BagEntity create(Level lvl, double x, double y, double z, int id) {
        BagEntity out = new BagEntity(R_TYPE.get(), lvl);
        out.moveTo(x, y, z);
        out.getPersistentData().putInt(BagItem.BAG_ID_KEY, id);
        lvl.addFreshEntity(out);
        return out;
    }

    public static BagEntity create(Level lvl, BlockPos pos, int id) {
        return create(lvl, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, id);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    public int getBagId() { return getPersistentData().getInt(BagItem.BAG_ID_KEY); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FALL) || source.is(DamageTypes.STALAGMITE)) //easter egg, a bag that receive cramming, fall or stalagmite damage will be auto equiped to a nearby entity TODO: (should probably put a damage cap for activation)
            level().getEntities(this, new AABB(blockPosition())).stream().findFirst().ifPresent(e->BagItem.equipBag(e, this));
        if (source.getEntity() != null || source.getDirectEntity() != null) {

            Entity sEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();

            if (sEntity != null && !sEntity.isRemoved()) {
                int id = getBagId();
                if (sEntity instanceof Player player) {
                    if (KeyMapController.SNEAK.getState(player))
                        BagItem.equipBag(player, this);
                    else {
                        PlayerUtils.giveOrDrop(player, BagItem.create(id));
                        this.remove(RemovalReason.KILLED);
                    }
                } else if (BagsData.runOnBag(id, b->b.isModulePresent(ParasiteModule.NAME), false))
                    BagItem.equipBag(sEntity, this);
            }
        }
        return false;
    }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {}

    public static List<ItemStack> getEmptyEquipment() { return EMPTY_EQUIPMENT; }

    @Override
    public Iterable<ItemStack> getHandSlots() { return EMPTY_EQUIPMENT; }

    @Override
    public Iterable<ItemStack> getArmorSlots() { return EMPTY_EQUIPMENT; }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (ForgeCapabilities.FLUID_HANDLER.equals(cap))
            return BagsData.runOnBag(this, BagInstance::tanksHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ITEM_HANDLER.equals(cap))
            return BagsData.runOnBag(this, BagInstance::slotsHandle, LazyOptional.empty()).cast();
        else if (ForgeCapabilities.ENERGY.equals(cap))
            return BagsData.runOnBag(this, BagInstance::energyHandle, LazyOptional.empty()).cast();
        else
            return LazyOptional.empty();
    }

    @Override
    public boolean isAlwaysTicking() { return true; }

    protected int loadCoolDown = 0;
    protected BlockPos lastPos = blockPosition();

    @Override
    public void tick() {
        if (!level().isClientSide && (--loadCoolDown <= 0 || !lastPos.equals(blockPosition()))) {
            World.temporaryChunkLoad(level(), blockPosition());
            loadCoolDown = 200;
            lastPos = blockPosition();
            BagsData.runOnBag(getBagId(), b->{
                if (getVehicle() != null)
                    b.setHolder(getVehicle());
                else
                    b.setHolder(this);
                b.temporaryChunkLoad();
            });
        }
        super.tick();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        //do corrections here
        Entity vehicle = getVehicle();
        if (vehicle instanceof Player player) {
            double dx = player.getX();
            double dy = player.getZ();
            double x = dx + 0;
            double y = dy + 0.45;
            double ang = -player.yBodyRot / 180. * Math.PI;
            double rx = (x - dx) * Math.cos(ang) - (y - dy) * Math.sin(ang) + dx;
            double ry = (x - dx) * Math.sin(ang) - (y - dy) * Math.cos(ang) + dy;
            setPos(rx, player.getY() + 0.8, ry);
//            setYBodyRot(player.getVisualRotationYInDegrees());
        }
        if (vehicle != null) {
//            setXRot(vehicle.getXRot());
//            setYRot(vehicle.getYRot());
        }
        if (vehicle instanceof LivingEntity living) {
            setYBodyRot(living.yBodyRot);
        }
    }

    @Override
    public boolean canPickUpLoot() { return false; }

    @Override
    public boolean canHoldItem(ItemStack p_175448_1_) { return false; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.level().isClientSide) return InteractionResult.SUCCESS;
        if (player.getItemInHand(hand).getItem() instanceof BlockItem bi && bi.getBlock() instanceof BaseModule module) {
            int id = getBagId();
            ItemStack stack = player.getItemInHand(hand);
            if (id > 0 && World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level) {
                BagsData.runOnBag(getBagId(), b->{
                    if (module.canInstall(b)) {
                        BlockPos pos = b.getAnyInstallPosition();
                        if (pos != null) {
                            level.setBlockAndUpdate(pos, module.defaultBlockState());
                            module.install(b, player, level, pos, stack);
                        }
                        if (!player.isCreative()) {
                            stack.shrink(1);
                            player.setItemInHand(hand, stack);
                        }
                    }
                });
                return InteractionResult.SUCCESS;
            }
        }
        if (KeyMapController.SNEAK.getState(player))
            BagsData.runOnBag(getBagId(), b->b.enter(player, false));
        else
            BagMenu.open(player, getBagId(), 0);
        return InteractionResult.SUCCESS;
    }
}
