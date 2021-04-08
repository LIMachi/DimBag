package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.common.items.FluidItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.ForgeFlowingFluid;
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
    public static final DeferredRegister<Fluid> FLUID_REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);

    public static final Map<String, RegistryObject<Block>> BLOCKS = new HashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEMS = new HashMap<>();
    public static final Map<String, RegistryObject<Fluid>> FLUIDS = new HashMap<>();
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
    public static Supplier<BlockItem> registerBlockItem(String name, String blockName, Item.Properties properties) {
        return registerItem(name, ()->new BlockItem(getBlock(blockName), properties));
    }

    /**
     * helper function to generate containers
     */
    public static void registerContainer(String name, net.minecraftforge.fml.network.IContainerFactory<? extends Container> factory) {
        registerContainerType(name, () -> IForgeContainerType.create(factory));
    }

//    public static Supplier<FlowingFluidBlock> registerFluidBlock(String name, String fluidName) {
//        BLOCKS.put(name, BLOCK_REGISTER.register(name, ()->new FlowingFluidBlock(fluid, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops())));
//    }

    /**
     * should be called in a static block
     */
    public static <F extends Fluid> Supplier<F> registerFluid(String name, Supplier<F> sup) {
        LOGGER.info("Registering Fluid: " + name);
        if (initializationFinished)
            LOGGER.warn("Trying to register a block after initialization phase! Please move this registration to static phase: " + name);
        else
            FLUIDS.put(name, FLUID_REGISTER.register(name, sup));
        return ()->getFluid(name);
    }

    /**
     * should be called in a static block
     */
    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> sup) {
        LOGGER.info("Registering Block: " + name);
        if (initializationFinished)
            LOGGER.warn("Trying to register a block after initialization phase! Please move this registration to static phase: " + name);
        else
            BLOCKS.put(name, BLOCK_REGISTER.register(name, sup));
        return ()->getBlock(name);
    }

    /**
     * should be called in a static block
     */
    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> sup) {
        LOGGER.info("Registering Item: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register an item after initialization phase! Please move this registration to static phase: " + name);
        else
            ITEMS.put(name, ITEM_REGISTER.register(name, sup));
        return ()->getItem(name);
    }

    /**
     * should be called in a static block
     */
    public static <T extends TileEntityType<?>> Supplier<T> registerTileEntityType(String name, Supplier<T> sup) {
        LOGGER.info("Registering TileEntityType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register a tile entity type after initialization phase! Please move this registration to static phase: " + name);
        else
            TILE_ENTITY_TYPES.put(name, TILE_ENTITY_REGISTER.register(name, sup));
        return ()->getTileEntityType(name);
    }

    /**
     * should be called in a static block
     */
    public static <T extends ContainerType<?>> Supplier<T> registerContainerType(String name, Supplier<T> sup) {
        LOGGER.info("Registering ContainerType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register a container type after initialization phase! Please move this registration to static phase: " + name);
        else
            CONTAINER_TYPES.put(name, CONTAINER_TYPE_REGISTER.register(name, sup));
        return ()->getContainerType(name);
    }

    /**
     * should be called in a static block
     */
    public static <T extends EntityType<?>> Supplier<T> registerEntityType(String name, Supplier<T> sup) {
        LOGGER.info("Registering EntityType: " + name);
        if (initializationFinished)
            LOGGER.error("Trying to register an entity type after initialization phase! Please move this registration to static phase: " + name);
        else
            ENTITY_TYPES.put(name, ENTITY_REGISTER.register(name, sup));
        return ()->getEntityType(name);
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
    public static <F extends Fluid> F getFluid(String name) {
        if (!initializationFinished)
            LOGGER.warn("Trying to access a registry object (of type fluid) before finishing initialization! Please move this access after the registry phase: " + name);
        return (F)FLUIDS.get(name).get();
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
        BLOCK_REGISTER.register(bus);
        ITEM_REGISTER.register(bus);
        TILE_ENTITY_REGISTER.register(bus);
        CONTAINER_TYPE_REGISTER.register(bus);
        ENTITY_REGISTER.register(bus);
        LOGGER.info("Finished registration");
        initializationFinished = true;
    }
}
