package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.blocks.*;
import com.limachi.dimensional_bags.common.container.*;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.TunnelPlacer;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.tileentities.BrainTileEntity;
import com.limachi.dimensional_bags.common.tileentities.GhostHandTileEntity;
import com.limachi.dimensional_bags.common.tileentities.PillarTileEntity;
import com.limachi.dimensional_bags.common.tileentities.TheEyeTileEntity;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<net.minecraft.tileentity.TileEntityType<?>> TILE_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final RegistryObject<Block> BAG_EYE_BLOCK = BLOCK_REGISTER.register("bag_eye", TheEye::new);
    public static final RegistryObject<Block> PILLAR_BLOCK = BLOCK_REGISTER.register("pillar", Pillar::new);
    public static final RegistryObject<Block> TUNNEL_BLOCK = BLOCK_REGISTER.register("tunnel", Tunnel::new);
    public static final RegistryObject<Block> WALL_BLOCK = BLOCK_REGISTER.register("wall", Wall::new);
    public static final RegistryObject<Block> CLOUD_BLOCK = BLOCK_REGISTER.register("cloud", Cloud::new);
    public static final RegistryObject<Block> BRAIN_BLOCK = BLOCK_REGISTER.register("brain", Brain::new);
    public static final RegistryObject<Block> GHOST_HAND_BLOCK = BLOCK_REGISTER.register("ghost_hand", GhostHand::new);

    public static final RegistryObject<Item> BAG_ITEM = ITEM_REGISTER.register("bag", Bag::new);
    public static final RegistryObject<Item> GHOST_BAG_ITEM = ITEM_REGISTER.register("ghost_bag", GhostBag::new);
    public static final RegistryObject<Item> TUNNEL_ITEM = ITEM_REGISTER.register("tunnel_placer", TunnelPlacer::new);

    public static RegistryObject<EntityType<BagEntityItem>> BAG_ITEM_ENTITY = ENTITY_REGISTER
            .register("bag_item", () -> EntityType.Builder.<BagEntityItem>create(BagEntityItem::new, EntityClassification.MISC)
                    .size(0.25F, 0.25F)
                    .build("bag_item"));

    public static final RegistryObject<Item> EYE_ITEM = ITEM_REGISTER.register("bag_eye", () -> new BlockItem(BAG_EYE_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));
    public static final RegistryObject<Item> PILLAR_ITEM = ITEM_REGISTER.register("pillar", () -> new BlockItem(PILLAR_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));
    public static final RegistryObject<Item> CLOUD_ITEM = ITEM_REGISTER.register("cloud", () -> new BlockItem(CLOUD_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));
    public static final RegistryObject<Item> BRAIN_ITEM = ITEM_REGISTER.register("brain", () -> new BlockItem(BRAIN_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));
    public static final RegistryObject<Item> WALL_ITEM = ITEM_REGISTER.register("wall", () -> new BlockItem(WALL_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));
    public static final RegistryObject<Item> GHOST_HAND_ITEM = ITEM_REGISTER.register("ghost_hand", () -> new BlockItem(GHOST_HAND_BLOCK.get(), new Item.Properties().group(DimBag.ITEM_GROUP)));

    public static final RegistryObject<TileEntityType<TheEyeTileEntity>> BAG_EYE_TE = TILE_ENTITY_REGISTER.register("bag_eye", () -> TileEntityType.Builder.create(TheEyeTileEntity::new, BAG_EYE_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<PillarTileEntity>> PILLAR_TE = TILE_ENTITY_REGISTER.register("pillar", () -> TileEntityType.Builder.create(PillarTileEntity::new, PILLAR_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<BrainTileEntity>> BRAIN_TE = TILE_ENTITY_REGISTER.register("brain", () -> TileEntityType.Builder.create(BrainTileEntity::new, BRAIN_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<GhostHandTileEntity>> GHOST_HAND_TE = TILE_ENTITY_REGISTER.register("ghost_hand", () -> TileEntityType.Builder.create(GhostHandTileEntity::new, GHOST_HAND_BLOCK.get()).build(null));

    public static final RegistryObject<ContainerType<BagContainer>> BAG_CONTAINER = CONTAINER_TYPE_REGISTER.register("inventory", () -> IForgeContainerType.create(BagContainer::CreateClient));
    public static final RegistryObject<ContainerType<WrappedPlayerInventoryContainer>> PLAYER_CONTAINER = CONTAINER_TYPE_REGISTER.register("player", () -> IForgeContainerType.create(WrappedPlayerInventoryContainer::new));
    public static final RegistryObject<ContainerType<BrainContainer>> BRAIN_CONTAINER = CONTAINER_TYPE_REGISTER.register("brain", () -> IForgeContainerType.create(BrainContainer::new));
    public static final RegistryObject<ContainerType<SettingsContainer>> SETTINGS_CONTAINER = CONTAINER_TYPE_REGISTER.register("settings", () -> IForgeContainerType.create(SettingsContainer::new));
    public static final RegistryObject<ContainerType<GhostHandContainer>> GHOST_HAND_CONTAINER = CONTAINER_TYPE_REGISTER.register("ghost_hand", () -> IForgeContainerType.create(GhostHandContainer::new));

    public static final RegistryObject<EntityType<BagEntity>> BAG_ENTITY = ENTITY_REGISTER.register("bag_entity", () -> EntityType.Builder.<BagEntity>create(BagEntity::new, EntityClassification.MISC).size(0.5f, 1f).build(new ResourceLocation(MOD_ID, "bag_entity").toString()));

    public static void registerAll(IEventBus bus) {
        UpgradeManager.registerItems(ITEM_REGISTER);
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        TILE_ENTITY_REGISTER.register(bus);
        CONTAINER_TYPE_REGISTER.register(bus);
        ENTITY_REGISTER.register(bus);
    }
}
