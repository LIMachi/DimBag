package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import com.limachi.dimensional_bags.common.tileentities.PillarTileEntity;
import net.minecraft.block.BlockState;
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

import javax.annotation.Nullable;
import java.util.function.Supplier;

@StaticInit
public class Pillar extends AbstractTileEntityBlock<PillarTileEntity> implements IHasBagSettings {

    public static final String NAME = "pillar";
    public static final Supplier<Pillar> INSTANCE = Registries.registerBlock(NAME, Pillar::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public Pillar() {
        super(NAME, Properties.create(Material.PISTON).hardnessAndResistance(1.5f, 3600000f).sound(SoundType.STONE), PillarTileEntity.class, PillarTileEntity.NAME);
    }

    @Override
    public <B extends AbstractTileEntityBlock<PillarTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ItemStack asItem(BlockState state, PillarTileEntity pillar) {
        ItemStack out = super.asItem(state, pillar);
        if (pillar != null && DimBag.isServer(null)) { //only run this part serve side as pillar.invId is server only
            PillarInventory inv = (PillarInventory) InventoryData.execute(pillar.getEyeId(), d -> d.getPillarInventory(pillar.getId()), null);
            if (out.getTag() == null)
                out.setTag(new CompoundNBT());
            out.getTag().remove("BlockEntityTag");
            out.getTag().merge(inv.serializeNBT());
        }
        return out;
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, PillarTileEntity pillar) {
        if (DimBag.isServer(worldIn)) { //only run this part server side as pillar.invId is server only
            CompoundNBT tags = stack.getTag();
            PillarInventory inv = new PillarInventory();
            if (tags != null && tags.contains("UUID"))
                inv.deserializeNBT(tags);
            InventoryData.execute(pillar.getEyeId(), d -> d.addPillar(inv));
            pillar.setId(inv.getId());
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, PillarTileEntity pillar) {
        EventManager.delayedTask(0, ()->InventoryData.execute(pillar.getEyeId(), d->d.removePillar(pillar.getId()))); //on remove is called before drops, rip
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int eyeId = DimBag.debug(SubRoomsManager.getEyeId(world, pos, false), "eye id");
        if (eyeId <= 0) return ActionResultType.SUCCESS;
        TileEntity te = DimBag.debug(world.getTileEntity(pos));
        if (!(te instanceof PillarTileEntity)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        new PillarContainer(0, player.inventory, eyeId, ((PillarTileEntity) te).getInventory().getId()).open(player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType openSettings(PlayerEntity player) { return ActionResultType.SUCCESS; }
}