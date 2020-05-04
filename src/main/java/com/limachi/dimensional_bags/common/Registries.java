package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.blocks.TheEye;
import com.limachi.dimensional_bags.common.container.BagContainer;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.container.UpgradeContainer;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.tileentities.TheEyeTileEntity;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = new DeferredRegister<>(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = new DeferredRegister<>(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<net.minecraft.tileentity.TileEntityType<?>> TILE_ENTITY_REGISTER = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE_REGISTER = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = new DeferredRegister<>(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<Block> BAG_EYE_BLOCK = BLOCK_REGISTER.register("bag_eye", TheEye::new);

    public static final RegistryObject<Item> BAG_ITEM = ITEM_REGISTER.register("bag", Bag::new);

    public static final RegistryObject<Item> EYE_ITEM = ITEM_REGISTER.register("bag_eye", () -> new BlockItem(BAG_EYE_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));

    public static final RegistryObject<TileEntityType<TheEyeTileEntity>> BAG_EYE_TE = TILE_ENTITY_REGISTER.register("bag_eye_te", () -> TileEntityType.Builder.create(TheEyeTileEntity::new, BAG_EYE_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<BagContainer>> BAG_CONTAINER = CONTAINER_TYPE_REGISTER.register("bag_container", () -> IForgeContainerType.create(BagContainer::new));
    public static final RegistryObject<ContainerType<UpgradeContainer>> UPGRADE_CONTAINER = CONTAINER_TYPE_REGISTER.register("upgrade_container", () -> IForgeContainerType.create(UpgradeContainer::new));

    public static void registerAll(IEventBus bus) {
        UpgradeManager.register(ITEM_REGISTER);
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        TILE_ENTITY_REGISTER.register(bus);
        CONTAINER_TYPE_REGISTER.register(bus);
        ENTITY_REGISTER.register(bus);
    }
}
