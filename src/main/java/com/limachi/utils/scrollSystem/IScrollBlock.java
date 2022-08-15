package com.limachi.utils.scrollSystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * can be implemented on block / blockentity
 * do remember that blocks are singleton and not blockentity when using 'this'
 */
public interface IScrollBlock {
    /**
     * called server side only, for calculation, only called when count down reached
     */
    void scroll(Level level, BlockPos pos, int delta, Player player);

    /**
     * called client side only, for visual/audio feedback, might be called a lot, delta should not be integrated
     */
    void scrollFeedBack(Level level, BlockPos pos, int delta, Player player);

    /**
     * called on both side to test if the scroll is locked on this block
     */
    boolean canScroll(Level level, BlockPos pos);
}
