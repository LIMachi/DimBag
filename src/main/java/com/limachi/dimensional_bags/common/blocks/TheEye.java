package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.function.Supplier;

@StaticInit
public class TheEye extends Block {

    public static final String NAME = "bag_eye";

    public static final Supplier<TheEye> INSTANCE = Registries.registerBlock(NAME, TheEye::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public TheEye() {
        super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.GLASS));
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int eyeId = SubRoomsManager.getEyeId(world, pos, true);
        if (eyeId <= 0)
            return ActionResultType.FAIL;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            EventManager.delayedTask(0, ()->SubRoomsManager.execute(eyeId, sm->sm.leaveBag(player, false, null, null)));
        else
            PillarContainer.open(player, eyeId, null);
//            Network.openEyeInventory((ServerPlayerEntity) player, eyeId, null);
            ; //FIXME
        return ActionResultType.SUCCESS;
    }
}
