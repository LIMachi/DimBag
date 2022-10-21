package com.limachi.dim_bag.moduleSystem;

import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.registries.StaticInit;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Supplier;

@StaticInit
public class TestModule {
    /*
    public static class TMBlock extends Block {
        @RegisterBlock
        public static Supplier<Block> R_BLOCK;

        public TMBlock() {
            super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.AMETHYST));
        }
    }
    public static class TMItem extends BlockItem implements IBagModuleItem {
        @RegisterItem
        public static Supplier<Item> R_ITEM;

        public TMItem() { super(TMBlock.R_BLOCK.get(), new Properties().stacksTo(1).tab(DimBag.INSTANCE.tab())); }
    }*/
    static {
        ModuleFactory.registerModule("test", new IModuleBehavior() {
            @Override
            public boolean install(boolean simulate, Player player, int bagId, ItemStack stack) {
                return true;
            }

            @Override
            public boolean listensTo(Class<? extends Event> event) {
                return VanillaGameEvent.class.equals(event);
            }

            @Override
            public void event(Event e) {
            }
        });
    }
}
