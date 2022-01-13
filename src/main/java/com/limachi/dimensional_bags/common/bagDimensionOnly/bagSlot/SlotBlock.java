package com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.events.EventManager;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import com.limachi.dimensional_bags.lib.common.blocks.BlockWithUUID;
import com.limachi.dimensional_bags.lib.common.blocks.IHasBagSettings;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@StaticInit
public class SlotBlock extends BlockWithUUID<SlotTileEntity> implements IHasBagSettings {

    public static final String NAME = "slot";
    public static final Supplier<SlotBlock> INSTANCE = Registries.registerBlock(NAME, SlotBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public SlotBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.STONE).noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never), SlotTileEntity.class, SlotTileEntity.NAME);
    }

    @Override
    public <B extends AbstractTileEntityBlock<SlotTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ItemStack asItem(BlockState state, SlotTileEntity pillar) {
        ItemStack out = super.asItem(state, pillar);
        if (pillar != null && DimBag.isServer(null)) {
            SlotInventory inv = pillar.getInventory();
            if (inv != null)
                out.getTag().merge(inv.serializeNBT());
        }
        return out;
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, SlotTileEntity pillar) {
        super.onValidPlace(worldIn, pos, state, placer, stack, pillar);
        if (DimBag.isServer(worldIn)) {
            CompoundNBT tags = stack.getTag();
            SlotInventory inv = new SlotInventory();
            if (tags != null && tags.contains("size") && tags.contains("stack"))
                inv.deserializeNBT(tags);
            else
                inv.setId(pillar.getUUID());
            InventoryData.execute(pillar.getbagId(), d -> d.addPillar(inv));
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, SlotTileEntity pillar) {
        EventManager.delayedTask(0, ()->InventoryData.execute(pillar.getbagId(), d->d.removePillar(pillar.getUUID()))); //on remove is called before drops, rip
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int bagId = DimBag.debug(SubRoomsManager.getbagId(world, pos, false), "eye id");
        if (bagId <= 0) return ActionResultType.SUCCESS;
        TileEntity te = DimBag.debug(world.getBlockEntity(pos));
        if (!(te instanceof SlotTileEntity)) return super.use(state, world, pos, player, hand, ray);
        SlotContainer.open(player, bagId, ((SlotTileEntity) te).getInventory().getId());
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType openSettings(PlayerEntity player, BlockPos pos) {
        if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
        int bagId = DimBag.debug(SubRoomsManager.getbagId(player.level, pos, false), "eye id");
        if (bagId <= 0) return ActionResultType.SUCCESS;
        TileEntity te = DimBag.debug(player.level.getBlockEntity(pos));
        if (!(te instanceof SlotTileEntity)) return ActionResultType.SUCCESS;
        SlotSettingsContainer.open(player, bagId, ((SlotTileEntity) te).getInventory().getId());
        return ActionResultType.SUCCESS;
    }
}