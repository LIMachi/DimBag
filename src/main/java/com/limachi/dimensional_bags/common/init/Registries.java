package com.limachi.dimensional_bags.common.init;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.blocks.BagEye;
import com.limachi.dimensional_bags.common.data.inventory.container.DimBagContainer;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.RowUpgrade;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = new DeferredRegister<>(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = new DeferredRegister<>(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<net.minecraft.tileentity.TileEntityType<?>> TILE_ENTITY_REGISTER = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE_REGISTER = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = new DeferredRegister<>(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<Block> BAG_EYE_BLOCK = BLOCK_REGISTER.register("bag_eye", BagEye::new);

    public static final RegistryObject<Item> BAG_ITEM = ITEM_REGISTER.register("bag", Bag::new);
    public static final RegistryObject<Item> BAG_EYE_ITEM = ITEM_REGISTER.register("bag_eye", () -> new BlockItem(BAG_EYE_BLOCK.get(), new Item.Properties().group(DimensionalBagsMod.ItemGroup.instance)));
    public static final RegistryObject<Item> UPGRADE_ROW = ITEM_REGISTER.register("row_upgrade", RowUpgrade::new);

    public static final RegistryObject<TileEntityType<BagEyeTileEntity>> BAG_EYE_TE = TILE_ENTITY_REGISTER.register("bag_eye_te", () -> TileEntityType.Builder.create(BagEyeTileEntity::new, BAG_EYE_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<DimBagContainer>> BAG_CONTAINER = CONTAINER_TYPE_REGISTER.register("bag_container", () -> IForgeContainerType.create(DimBagContainer::new));

    public static final RegistryObject<EntityType<BagEntity>> BAG_ENTITY = ENTITY_REGISTER.register("bag_entity", () -> EntityType.Builder.<BagEntity>create(BagEntity::new, EntityClassification.MISC).size(0.5f, 1f).build(new ResourceLocation(MOD_ID, "bag_entity").toString()));

    public static void registerAll() {
        IEventBus meb = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCK_REGISTER.register(meb);
        ITEM_REGISTER.register(meb);
        TILE_ENTITY_REGISTER.register(meb);
        CONTAINER_TYPE_REGISTER.register(meb);
        ENTITY_REGISTER.register(meb);
    }
}
