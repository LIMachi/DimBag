package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.modes.Default;
import com.limachi.dimensional_bags.common.tileentities.BagProxyTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@StaticInit
public class BagProxy extends AbstractTileEntityBlock<BagProxyTileEntity> {

    public static final Item OP_MODULE = Items.NETHER_STAR; //TODO: use another item instead

    public static final String NAME = "bag_proxy";

    public static final Supplier<BagProxy> INSTANCE = Registries.registerBlock(NAME, BagProxy::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public BagProxy() {
        super(NAME, Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(1.5f, 3600000f).sound(SoundType.GLASS), BagProxyTileEntity.class, BagProxyTileEntity.NAME);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public <B extends AbstractTileEntityBlock<BagProxyTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, BagProxyTileEntity tileEntity) {

    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, BagProxyTileEntity tileEntity) {

    }

    /**
     * behavior:
     * if op and hand contains bag -> overide the id of the proxy
     * if not op and hand contains bag -> consume bag and store id
     * if not op and hand contains op module -> consume module and set op (if id alrady set, give/drop bag with id)
     * other (not op, no valid item in hand) -> use the Default mode right click (simple click: open inventory, shift click: teleport)
     */
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof BagProxyTileEntity) {
            BagProxyTileEntity proxy = (BagProxyTileEntity) te;
            ItemStack held = player.getHeldItem(handIn);
            boolean isBag = held.getItem() instanceof Bag; //note: this might be a ghost bag
            if (proxy.isOP()) {
                if (isBag) {
                    proxy.setID(Bag.getEyeId(held));
                    return ActionResultType.SUCCESS;
                }
            } else {
                int eye = proxy.eyeId();
                if (isBag) {
                    int id = Bag.getEyeId(held);
                    List<CuriosIntegration.ProxyItemStackModifier> res = CuriosIntegration.searchItem(player, Bag.class, o->(!(o.getItem() instanceof GhostBag) && Bag.getEyeId(o) == id), true);
                    if (!res.isEmpty()) { //this might be empty if in hand we have a ghost bag but no bags (player in bag using the bag key)
                        proxy.setID(id);
                        res.get(0).set(ItemStack.EMPTY);
                    }
                } else if (held.getItem().equals(OP_MODULE)) {
                    proxy.setOP(true);
                    if (eye != 0)
                        Bag.giveBag(eye, player);
                    held.shrink(1);
                    player.setHeldItem(handIn, held);
                }
                return ActionResultType.SUCCESS;
            }
            int eye = proxy.eyeId();
            if (eye > 0)
                return ModeManager.getMode(Default.ID).onItemRightClick(eye, worldIn, player);
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    /**
     * behavior: if not op and id set: give back bag and clear id
     * if op: give back OP item and clear id
     */
    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof BagProxyTileEntity) {
            BagProxyTileEntity proxy = (BagProxyTileEntity) te;
            if (proxy.isOP()) {
                ItemStack out = new ItemStack(OP_MODULE);
                if (!player.addItemStackToInventory(out))
                    player.dropItem(out, false);
                proxy.setOP(false);
                proxy.setID(0);
            } else {
                int eye = proxy.eyeId();
                if (eye > 0) {
                    Bag.giveBag(eye, player);
                    proxy.setID(0);
                }
            }
        }
        super.onBlockClicked(state, worldIn, pos, player);
    }
}
