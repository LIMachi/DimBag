package com.limachi.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class Registries {
    public static final UUID NULL_UUID = new UUID(0, 0);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RegisterItem {
        java.lang.String name() default "";
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RegisterBlock {
        java.lang.String name() default "";
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RegisterBlockItem {
        java.lang.String name() default "";
        java.lang.String block();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface RegisterEntity {
        java.lang.String name() default "";
        net.minecraft.world.entity.MobCategory category() default MobCategory.MISC;
        float width() default 1f;
        float height() default 1f;
    }

    private static void discoverBlockRegistry(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterBlock.class)) {
            String name = name(a, "block");
            Field f = a.getAnnotatedField();
            try {
                f.set(null, block(modId, name, ()-> {
                    try {
                        return (Block)(a.getAnnotatedClass().getConstructor().newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                        return null;
                    }
                }));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private static void discoverItemRegistry(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterItem.class)) {
            a.setAnnotatedStaticFieldData(item(modId, name(a, "item"), ()-> {
                try {
                    return (Item)(a.getAnnotatedClass().getConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }
            }));
        }
    }

    private static void discoverBlockItemRegistry(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterBlockItem.class)) {
            String name = name(a, "block");
            Field f = a.getAnnotatedField();
            Field bf = a.getFieldFromAnnotatedClass(a.getData("block", ""));
            try {
                f.set(null, item(modId, name, ()->{try {
                    return new BlockItem(((Supplier<Block>)bf.get(null)).get(), ModBase.INSTANCES.get(modId).defaultProps());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }}));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private static void discoverEntityRegistry(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterEntity.class)) {
            a.setAnnotatedStaticFieldData(entity(modId, name(a, "entity"), (type, level)-> {
                try {
                    return (Entity)a.getAnnotatedClass().getConstructor(EntityType.class, Level.class).newInstance(type, level);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                }
            }, a.getData("category", MobCategory.MISC), a.getData("width", 1f), a.getData("height", 1f)));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EntityAttributeBuilder {
        java.lang.String name() default "";
    }

    private static void discoverEntityAttributeBuilder(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, EntityAttributeBuilder.class)) {
            String name = name(a, "entity");
            registerEntityAttributes(modId, name, (Class<? extends EntityType<? extends LivingEntity>>)ForgeRegistries.ENTITIES.getValue(new ResourceLocation(modId, name)).getClass(), ()->a.invokeStaticAnnotatedMethod());
        }
    }

    private static ArrayList<Reab> ATTRIBUTES = new ArrayList<>();

    private static record Reab(String modId, String regKey, Class<? extends EntityType<? extends LivingEntity>> clazz, Supplier<AttributeSupplier.Builder> builder) {
        void register(EntityAttributeCreationEvent event) { event.put(getRegisteredObject(modId, regKey, clazz), builder.get().build()); }
    }

    public static void registerEntityAttributes(String modId, String regKey, Class<? extends EntityType<? extends LivingEntity>> entityTypeClass, Supplier<AttributeSupplier.Builder> builder) {
        ATTRIBUTES.add(new Reab(modId, regKey, entityTypeClass, builder));
    }

    private static String name(ModAnnotation a, String remove) {
        String name = a.getData("name", "");
        if (name.equals(""))
            name = Strings.camel_to_snake(Strings.get_file('.', a.getAnnotatedClass().getCanonicalName())).replace("_" + remove, "");
        return name;
    }

    private static void runRegisterAnnotationDiscovery(String modId) {
        discoverBlockRegistry(modId);
        discoverItemRegistry(modId);
        discoverBlockItemRegistry(modId);
        discoverEntityAttributeBuilder(modId);
        discoverEntityRegistry(modId);
    }

    protected static final HashMap<String, HashMap<Class<?>, DeferredRegister<?>>> REGISTRIES = new HashMap<>();
    protected static final HashMap<Class<?>, IForgeRegistry<?>> MAPPED_REGISTRIES = new HashMap<>();
    protected static Map.Entry<Class<?>, IForgeRegistry<?>> getEntry(Class<?> clazz) {
        for (Map.Entry<Class<?>, IForgeRegistry<?>> e : MAPPED_REGISTRIES.entrySet()) {
            if (e.getKey().isAssignableFrom(clazz))
                return e;
        }
        return null;
    }
    static {
        MAPPED_REGISTRIES.put(Block.class, ForgeRegistries.BLOCKS);
        MAPPED_REGISTRIES.put(Fluid.class, ForgeRegistries.FLUIDS);
        MAPPED_REGISTRIES.put(Item.class, ForgeRegistries.ITEMS);
        MAPPED_REGISTRIES.put(MobEffect.class, ForgeRegistries.MOB_EFFECTS);
        MAPPED_REGISTRIES.put(SoundEvent.class, ForgeRegistries.SOUND_EVENTS);
        MAPPED_REGISTRIES.put(Potion.class, ForgeRegistries.POTIONS);
        MAPPED_REGISTRIES.put(Enchantment.class, ForgeRegistries.ENCHANTMENTS);
        MAPPED_REGISTRIES.put(EntityType.class, ForgeRegistries.ENTITIES);
        MAPPED_REGISTRIES.put(BlockEntityType.class, ForgeRegistries.BLOCK_ENTITIES);
        MAPPED_REGISTRIES.put(ParticleType.class, ForgeRegistries.PARTICLE_TYPES);
        MAPPED_REGISTRIES.put(MenuType.class, ForgeRegistries.CONTAINERS);
        MAPPED_REGISTRIES.put(Motive.class, ForgeRegistries.PAINTING_TYPES);
        MAPPED_REGISTRIES.put(RecipeSerializer.class, ForgeRegistries.RECIPE_SERIALIZERS);
        MAPPED_REGISTRIES.put(Attribute.class, ForgeRegistries.ATTRIBUTES);
        MAPPED_REGISTRIES.put(StatType.class, ForgeRegistries.STAT_TYPES);
        MAPPED_REGISTRIES.put(VillagerProfession.class, ForgeRegistries.PROFESSIONS);
        MAPPED_REGISTRIES.put(PoiType.class, ForgeRegistries.POI_TYPES);
        MAPPED_REGISTRIES.put(MemoryModuleType.class, ForgeRegistries.MEMORY_MODULE_TYPES);
        MAPPED_REGISTRIES.put(SensorType.class, ForgeRegistries.SENSOR_TYPES);
        MAPPED_REGISTRIES.put(Schedule.class, ForgeRegistries.SCHEDULES);
        MAPPED_REGISTRIES.put(Activity.class, ForgeRegistries.ACTIVITIES);
        MAPPED_REGISTRIES.put(WorldCarver.class, ForgeRegistries.WORLD_CARVERS);
        MAPPED_REGISTRIES.put(Feature.class, ForgeRegistries.FEATURES);
        MAPPED_REGISTRIES.put(ChunkStatus.class, ForgeRegistries.CHUNK_STATUS);
        MAPPED_REGISTRIES.put(StructureFeature.class, ForgeRegistries.STRUCTURE_FEATURES);
        MAPPED_REGISTRIES.put(BlockStateProviderType.class, ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES);
        MAPPED_REGISTRIES.put(FoliagePlacerType.class, ForgeRegistries.FOLIAGE_PLACER_TYPES);
        MAPPED_REGISTRIES.put(TreeDecoratorType.class, ForgeRegistries.TREE_DECORATOR_TYPES);
        MAPPED_REGISTRIES.put(Biome.class, ForgeRegistries.BIOMES);
    }

    protected static <T> DeferredRegister<T> getRegistry(String modId, Class<T> clazz) {
        Map.Entry<Class<?>, IForgeRegistry<?>> e = getEntry(clazz);
        if (e == null) {
            Log.error("invalid class: " + clazz);
            return null;
        }
        if (!REGISTRIES.containsKey(modId)) { REGISTRIES.put(modId, new HashMap<>()); }
        HashMap<Class<?>, DeferredRegister<?>> mr = REGISTRIES.get(modId);
        if (!mr.containsKey(e.getKey())) {
            mr.put(e.getKey(), DeferredRegister.create(e.getValue(), modId));
        }
        return (DeferredRegister<T>)mr.get(clazz);
    }

    public static <T extends ForgeRegistryEntry<?>> T getRegisteredObject(String modId, String regKey, Class<T> clazz) {
        Map.Entry<Class<?>, IForgeRegistry<?>> reg = getEntry(clazz);
        if (reg == null) return null;
        return (T)reg.getValue().getValue(new ResourceLocation(modId, regKey));
    }

    public static <T extends Block> RegistryObject<T> block(String modId, String regKey, Supplier<T> blockNew) { return getRegistry(modId, Block.class).register(regKey, blockNew); }
    public static <T extends Item> RegistryObject<T> item(String modId, String regKey, Supplier<T> itemNew) { return getRegistry(modId, Item.class).register(regKey, itemNew); }
    public static <T extends Block> Pair<RegistryObject<BlockItem>, RegistryObject<T>> blockAndItem(String modId, String regKey, Supplier<T> blockNew, Item.Properties props) {
        RegistryObject<T> r_block = getRegistry(modId, Block.class).register(regKey, blockNew);
        return Pair.of(getRegistry(modId, Item.class).register(regKey, ()->new BlockItem(r_block.get(), props)), r_block);
    }
    public static <T extends Entity> RegistryObject<EntityType<T>> entity(String modId, String regKey, EntityType.EntityFactory<T> eNew, MobCategory category, float width, float height) { return entity(modId, regKey, ()->EntityType.Builder.of(eNew, category).sized(width, height).build(regKey)); }
    public static <T extends Entity> RegistryObject<EntityType<T>> entity(String modId, String regKey, Supplier<EntityType<T>> etNew) { return getRegistry(modId, EntityType.class).register(regKey, etNew); }
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String modId, String regKey, BlockEntityType.BlockEntitySupplier<T> beNew, RegistryObject<Block> block) { return blockEntity(modId, regKey, beNew, block, null); }
    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String modId, String regKey, BlockEntityType.BlockEntitySupplier<T> beNew, RegistryObject<Block> block, com.mojang.datafixers.types.Type<?> fixer) { return getRegistry(modId, BlockEntityType.class).register(regKey, ()->BlockEntityType.Builder.of(beNew, block.get()).build(fixer)); }
    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String modId, String regKey, MenuType.MenuSupplier<T> menuNew) { return getRegistry(modId, MenuType.class).register(regKey, ()->new MenuType<T>(menuNew)); }

    public static void annotations(String modId) {
        try {
            runRegisterAnnotationDiscovery(modId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(String modId) {
        HashMap<Class<?>, DeferredRegister<?>> mr = REGISTRIES.get(modId);
        if (mr == null) {
            Log.warn("no registry found for mod id: " + modId);
            return;
        }
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(Registries.class);
        for (DeferredRegister<?> reg : mr.values()) {
            reg.register(bus);
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        for (Reab e : ATTRIBUTES) e.register(event);
    }
}
