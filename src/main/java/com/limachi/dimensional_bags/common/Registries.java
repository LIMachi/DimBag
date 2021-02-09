package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.inventory.container.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.datafixers.types.Type;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.DimBag.LOGGER;
import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class Registries {
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<net.minecraft.tileentity.TileEntityType<?>> TILE_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);

    public static final Map<String, RegistryObject<Block>> BLOCKS = new HashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEMS = new HashMap<>();
    public static final Map<String, RegistryObject<net.minecraft.tileentity.TileEntityType<?>>> TILE_ENTITY_TYPES = new HashMap<>();
    public static final Map<String, RegistryObject<ContainerType<?>>> CONTAINER_TYPES = new HashMap<>();
    public static final Map<String, RegistryObject<EntityType<?>>> ENTITY_TYPES = new HashMap<>();

    protected static boolean initializationFinished = false;

    /**
     * helper function to generate a tile entity type and bind a tile entity to a block
     */
    public static void registerTileEntity(String name, Supplier<? extends TileEntity> teSup, Supplier<? extends Block> blockSup, @Nullable Type<?> fixer) {
        registerTileEntityType(name, ()-> TileEntityType.Builder.create(teSup, blockSup.get()).build(fixer));
    }

    /**
     * helper function to generate block items
     */
    public static void registerBlockItem(String name, String blockName, Item.Properties properties) {
        registerItem(name, ()->new BlockItem(getBlock(blockName), properties));
    }

    /**
     * helper function to generate containers
     */
    public static void registerContainer(String name, net.minecraftforge.fml.network.IContainerFactory<? extends Container> factory) {
        registerContainerType(name, () -> IForgeContainerType.create(factory));
    }

    /**
     * should be called in a static block
     */
    public static void registerBlock(String name, Supplier<? extends Block> sup) {
        LOGGER.info("Registering Block: " + name);
        if (initializationFinished)
            LOGGER.warn("Trying to register a block after initialization phase! Please move this registration to static phase: " + name);
        else
            BLOCKS.put(name, BLOCK_REGISTER.register(name, sup));
    }

    /**
     * should be called in a static block
     */
    public static void registerItem(String name, Supplier<? extends Item> sup) {
        LOGGER.info("Registering Item: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register an item after initialization phase! Please move this registration to static phase: " + name);
        else
            ITEMS.put(name, ITEM_REGISTER.register(name, sup));
    }

    /**
     * should be called in a static block
     */
    public static void registerTileEntityType(String name, Supplier<? extends net.minecraft.tileentity.TileEntityType<?>> sup) {
        LOGGER.info("Registering TileEntityType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register a tile entity type after initialization phase! Please move this registration to static phase: " + name);
        else
            TILE_ENTITY_TYPES.put(name, TILE_ENTITY_REGISTER.register(name, sup));
    }

    /**
     * should be called in a static block
     */
    public static void registerContainerType(String name, Supplier<? extends ContainerType<?>> sup) {
        LOGGER.info("Registering ContainerType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register a container type after initialization phase! Please move this registration to static phase: " + name);
        else
            CONTAINER_TYPES.put(name, CONTAINER_TYPE_REGISTER.register(name, sup));
    }

    /**
     * should be called in a static block
     */
    public static void registerEntityType(String name, Supplier<? extends EntityType<?>> sup) {
        LOGGER.info("Registering EntityType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register an entity type after initialization phase! Please move this registration to static phase: " + name);
        else
            ENTITY_TYPES.put(name, ENTITY_REGISTER.register(name, sup));
    }

    /**
     * only valid after register phase
     */
    public static <B extends Block> B getBlock(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type block) before finishing initialization! Please move this access after the registry phase: " + name);
        return (B)BLOCKS.get(name).get();
    }

    /**
     * only valid after register phase
     */
    public static <I extends Item> I getItem(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type item) before finishing initialization! Please move this access after the registry phase: " + name);
        return (I)ITEMS.get(name).get();
    }

    /**
     * only valid after register phase
     */
    public static <T extends net.minecraft.tileentity.TileEntityType<?>> T getTileEntityType(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type tile entity type) before finishing initialization! Please move this access after the registry phase: " + name);
        return (T) TILE_ENTITY_TYPES.get(name).get();
    }

    /**
     * only valid after register phase
     */
    public static <T extends ContainerType<?>> T getContainerType(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type container type) before finishing initialization! Please move this access after the registry phase: " + name);
        return (T)CONTAINER_TYPES.get(name).get();
    }

    /**
     * only valid after register phase
     */
    public static <T extends EntityType<?>> T getEntityType(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type entity type) before finishing initialization! Please move this access after the registry phase: " + name);
        return (T) ENTITY_TYPES.get(name).get();
    }

    public static void registerAll(IEventBus bus) {
        UpgradeManager.registerItems(ITEM_REGISTER);
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        TILE_ENTITY_REGISTER.register(bus);
        CONTAINER_TYPE_REGISTER.register(bus);
        ENTITY_REGISTER.register(bus);
        LOGGER.info("Finished registration");
        initializationFinished = true;
    }
}
