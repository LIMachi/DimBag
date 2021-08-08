package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class BagEntity extends MobEntity implements IEyeIdHolder {

    public static final String NAME = "bag_entity";

    static {
        Registries.registerEntityType(NAME, () -> EntityType.Builder.<BagEntity>create(BagEntity::new, EntityClassification.MISC).size(0.5f, 1f).build(new ResourceLocation(MOD_ID, "bag_entity").toString()));
    }

    public static String ITEM_KEY = "BagItemStack";

    public BagEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute getAttributeMap() {
        return LivingEntity.registerAttributes().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public void onDeath(DamageSource cause) {
        LOGGER.error("Something tried to remove this bag without authorization: " + getEyeId() + " (cause: " + cause + ")");
    }

    @Override
    public void onKillCommand() {}

    @Override
    protected void outOfWorld() {}

    public static BagEntity spawn(World world, BlockPos position, ItemStack bag) {
        BagEntity out = new BagEntity(Registries.getEntityType(NAME), world);
        out.setPosition(position.getX() + 0.5d, position.getY() + 0.5d, position.getZ() + 0.5d);
        out.setInvulnerable(true);
        out.enablePersistence();
        out.getPersistentData().put(ITEM_KEY, bag.write(new CompoundNBT()));
        out.setCustomName(bag.getDisplayName());
        out.setCustomNameVisible(true);
        world.addEntity(out);
        return out;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    protected boolean canTriggerWalking() { return false; } //prevent walking (meh)

    @Override
    public void applyEntityCollision(Entity entityIn) {
        DimBag.LOGGER.info("bag collided with " + entityIn);
    } //prevent pushing (at least, i though it was, rip)

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(Items.AIR); //no spawn egg for you my good lad/lass
    }

    @Override
    public boolean canBeCollidedWith() { return true; } //if false, prevent punch :(

    @Override
    public boolean canBePushed() { return false; } //don't seem to work, sad :(

    @Override
    public boolean shouldRiderSit() { return false; }

    @Override
    public boolean canRiderInteract() { return true; }

    @Override
    public boolean canBeRiddenInWater(Entity rider) { return true; }

    @Override
    public EntityClassification getClassification(boolean forSpawnCount) { return null; }

    @Override
    public void tick() {
        super.tick();
        int eyeId = getEyeId();
        if (eyeId > 0) {
            if (DimBag.isServer(world))
                HolderData.execute(eyeId, holderData->holderData.setHolder(this));
            ModeManager.execute(eyeId, modeManager -> modeManager.inventoryTick(world, this, true));
            UpgradeManager.execute(eyeId, upgradeManager -> upgradeManager.inventoryTick(world, this));
        }
    }

    @Override
    protected ActionResultType getEntityInteractionResult(PlayerEntity player, Hand hand) {
        if (player.world.isRemote()) return ActionResultType.PASS;
        int id = getEyeId();
        if (id == 0) return ActionResultType.PASS;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            SubRoomsManager.execute(id, sm->sm.enterBag(player));
        } else
            new PillarContainer(0, player.inventory, getEyeId(), null).open(player);
        return ActionResultType.SUCCESS;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {

    }

    public ItemStack getBagItem() {
        return ItemStack.read(getPersistentData().getCompound(ITEM_KEY));
    }

    @Override
    public int getEyeId() {
        return Bag.getEyeId(getBagItem());
    }
}
