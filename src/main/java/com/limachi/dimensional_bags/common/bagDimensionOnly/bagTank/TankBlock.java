package com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank;

import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.events.EventManager;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
import com.limachi.dimensional_bags.common.bag.modes.Tank;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@StaticInit
public class TankBlock extends AbstractTileEntityBlock<TankTileEntity> {

    public static final String NAME = "tank";
    public static final Supplier<TankBlock> INSTANCE = Registries.registerBlock(NAME, TankBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public TankBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.STONE).noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never).lightLevel(state->5), TankTileEntity.class, TankTileEntity.NAME);
    }

    @Override
    public <B extends AbstractTileEntityBlock<TankTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ItemStack asItem(BlockState state, TankTileEntity fountain) {
        ItemStack out = super.asItem(state, fountain);
        if (fountain != null && DimBag.isServer(null)) { //only run this part serve side as fountain.invId is server only
            TankInventory tank = (TankInventory) TankData.execute(fountain.getbagId(), d -> d.getFountainTank(fountain.getId()), null);
            if (out.getTag() == null)
                out.setTag(new CompoundNBT());
            out.getTag().remove("BlockEntityTag");
            out.getTag().merge(tank.serializeNBT());
        }
        return out;
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, TankTileEntity fountain) {
        if (DimBag.isServer(worldIn)) { //only run this part server side as fountain.id is server only
            CompoundNBT tags = stack.getTag();
            TankInventory tank = new TankInventory();
            if (tags != null && tags.contains("UUID"))
                tank.deserializeNBT(tags);
            TankData.execute(fountain.getbagId(), d -> d.addFountain(tank));
            fountain.setId(tank.getId());
        }
        else { //FIXME
            CompoundNBT tags = stack.getTag();
            if (tags != null && tags.contains("UUID")) {
                TankInventory tank = new TankInventory();
                tank.deserializeNBT(tags);
                fountain.setId(tank.getId());
            }
        }
        ModeManager.execute(SubRoomsManager.getbagId(worldIn, pos, false), mm->mm.installMode(Tank.ID));
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, TankTileEntity fountain) {
        EventManager.delayedTask(0, ()->TankData.execute(fountain.getbagId(), d->d.removeFountain(fountain.getId()))); //on remove is called before drops, rip
    }

    @Config(cmt = "does clicking a fountain with a tank item (bucket, glass bottle, etc...) should empty or fill it (set to false if you want players to be forced to use strict fluid mechanics)")
    public static final boolean CAN_FILL_AND_EMPTY_ITEM_TANKS = true;

    @Nonnull
    @Override
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int bagId = DimBag.debug(SubRoomsManager.getbagId(world, pos, false), "eye id");
        if (bagId <= 0) return ActionResultType.SUCCESS;
        TileEntity te = DimBag.debug(world.getBlockEntity(pos));
        if (!(te instanceof TankTileEntity)) return super.use(state, world, pos, player, hand, ray);
        TankInventory tank = ((TankTileEntity) te).getTank();
        if (CAN_FILL_AND_EMPTY_ITEM_TANKS) {
            ItemStack out = Tank.stackInteraction(player.getItemInHand(hand), tank, player.inventory);
            if (!out.equals(player.getItemInHand(hand), false)) {
                player.setItemInHand(hand, out);
                return ActionResultType.SUCCESS;
            }
        }
//        DimBag.debug(tank, "expected tank").open((ServerPlayerEntity) DimBag.debug(player));
        TankContainer.open(player, bagId, tank.getId());
        return ActionResultType.SUCCESS;
    }
}