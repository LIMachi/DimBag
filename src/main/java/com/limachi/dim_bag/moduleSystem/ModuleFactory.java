package com.limachi.dim_bag.moduleSystem;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.registries.Registries;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ModuleFactory {
    private static final HashMap<String, Pair<RegistryObject<Block>, RegistryObject<Item>>> MODULES_REGISTRIES = new HashMap<>();
    private static final HashMap<String, IModuleBehavior> MODULES = new HashMap<>();
    private static final HashMap<String, RegistryObject<BlockEntityType<?>>> MODULES_BET_REGISTRIES = new HashMap<>();
    private static final Pair<RegistryObject<Block>, RegistryObject<Item>> MISSING_MODULE = new Pair<>(null, null);

    public static RegistryObject<Block> getBlock(String id) { return MODULES_REGISTRIES.getOrDefault(id, MISSING_MODULE).getFirst(); }
    public static RegistryObject<Item> getItem(String id) { return MODULES_REGISTRIES.getOrDefault(id, MISSING_MODULE).getSecond(); }
    public static RegistryObject<BlockEntityType<?>> getBlockEntityType(String id) { return MODULES_BET_REGISTRIES.get(id); }
    public static IModuleBehavior getBehavior(String id) { return MODULES.get(id); }

    /**
     * auto generate a block, item and eventually BlockEntity (if module extends IBlockEntityModuleBehavior)
     */
    public static void registerModule(String id, IModuleBehavior module) {
        RegistryObject<Block> rBlock;
        if (module instanceof IBlockEntityModuleBehavior) {

            class TBlockEntity extends BlockEntity {

                public TBlockEntity(BlockPos pos, BlockState state) {
                    super(MODULES_BET_REGISTRIES.get(id).get(), pos, state);
                }
            }

            class TBlock1 extends Block implements IBagModuleBlock, EntityBlock {
                public TBlock1() {
                    super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.ANCIENT_DEBRIS));
                }

                @Override
                public String getModuleId() {
                    return id;
                }

                @Nullable
                @Override
                public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                    return new TBlockEntity(pos, state);
                }
            }

            rBlock = Registries.block(Constants.MOD_ID, id, TBlock1::new);

            MODULES_BET_REGISTRIES.put(id, (RegistryObject<BlockEntityType<?>>)(Object)Registries.blockEntity(Constants.MOD_ID, id, TBlockEntity::new, rBlock));
        } else {

            class TBlock2 extends Block implements IBagModuleBlock {
                public TBlock2() {
                    super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.ANCIENT_DEBRIS));
                }

                @Override
                public String getModuleId() {
                    return id;
                }
            }

            rBlock = Registries.block(Constants.MOD_ID, id, TBlock2::new);
        }

        class TItem extends BlockItem implements IBagModuleItem {
            public TItem() {
                super(rBlock.get(), new Properties().stacksTo(1).tab(DimBag.INSTANCE.tab()));
            }
        }

        RegistryObject<Item> rItem = Registries.item(Constants.MOD_ID, id, TItem::new, "jei.info." + id);
        MODULES_REGISTRIES.put(id, new Pair<>(rBlock, rItem));
        MODULES.put(id, module);
    }
}
