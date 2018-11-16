package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public interface IWorkProvider<T extends TargetInfo> extends ITargetProvider {
    /**
     * Get the location of the work provider, for use in
     * distance calculations.
     */
    BlockPosDimension getPosition();

    /**
     * Whether or not blocks can potentially be handled no
     * matter if they have an associated tile entity or not.
     */
    boolean shouldProcessBlocks();

    /**
     * Whether or not tile entities can potentially be handled.
     */
    boolean shouldProcessTiles();

    /**
     * Whether or not inventory contents can potentially be handled.
     */
    boolean shouldProcessItems();

    /**
     * Create an instance of TargetInfo for the given target.
     * This is called when unable to perform work on a block itself,
     * but able to perform work on an item within that block.
     *
     * @param target The location of the prospective target.
     * @return
     */
    T createInfo(@Nonnull BlockPosDimension target);

    /**
     * Determine whether or not we can perform work on the provided
     * block and tile entity. Expected to return an instance of TargetInfo
     * if we can work, otherwise null.
     *
     * @param target The location of the prospective target.
     * @param world  The world the target is in.
     * @param block  The block state of the prospective target.
     * @param tile   The tile entity of the prospective target. May be null if getProcessBlocks returns true.
     * @return An instance of TargetInfo if we can work on that target, otherwise false.
     */
    T canWork(@Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, TileEntity tile);

    /**
     * Determine whether or not we can perform work on the provided
     * item stack.
     *
     * @param stack     The item stack.
     * @param slot      Which slot of the inventory it's in.
     * @param inventory The inventory containing the item stack.
     * @param target    The location of the target containing the item stack.
     * @param world     The world the target is in.
     * @param block     The block state of the target containing the item stack.
     * @param tile      The tile entity of the target containing the item stack.
     * @return True if we should try working on that item stack.
     */
    boolean canWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull BlockPosDimension target, @Nonnull World world, @Nonnull IBlockState block, @Nonnull TileEntity tile);

    /**
     * Attempt to perform work on the provided target.
     *
     * @param target The TargetInfo returned from canWork.
     * @param world  The world the target is in.
     * @param state  The block state of the target.
     * @param tile   The tile entity of the target.
     * @return
     */
    @Nonnull
    WorkResult performWork(@Nonnull T target, @Nonnull World world, @Nonnull IBlockState state, TileEntity tile);

    /**
     * Attempt to perform work on the provided item stack for
     * inventory processing.
     *
     * @param stack     The item stack to process.
     * @param slot      The slot the item stack is in.
     * @param inventory The inventory containing the item stack.
     * @param target    A TargetInfo instance for the target containing this item.
     * @param world     The world the target is in.
     * @param state     The block state of the target's containing block.
     * @param tile      The tile entity of the target's containing block.
     * @return
     */
    @Nonnull
    WorkResult performWork(@Nonnull ItemStack stack, int slot, @Nonnull IItemHandler inventory, @Nonnull T target, @Nonnull World world, @Nonnull IBlockState state, @Nonnull TileEntity tile);


    enum WorkResult {
        /**
         * This work item was skipped and should not count against the maximum
         * operations per tick.
         */
        SKIPPED(0, false, true, false),

        /**
         * Work was a success. This operation should count against the maximum
         * operations per tick and work processing should continue.
         */
        SUCCESS_CONTINUE(1, true, true, false),

        /**
         * Work was a failure. This operation should count against the maximum
         * operations per tick and work processing should continue.
         */
        FAILURE_CONTINUE(1, false, true, false),

        /**
         * Work was a success. Work can no longer be performed on this target
         * and the target should be removed from the work list.
         */
        SUCCESS_REMOVE(1, true, true, true),

        /**
         * Work was a failure. Work is not currently possible on this target
         * and the target should be removed from the work list.
         */
        FAILURE_REMOVE(1, false, true, true),

        /**
         * Work was a success. We should stop working though, so stop iterating.
         */
        SUCCESS_STOP(1, true, false, false),

        /**
         * Work was a failure. We should also stop working, so stop iterating.
         */
        FAILURE_STOP(1, false, false, false),

        SUCCESS_STOP_REMOVE(1, true, false, true),
        FAILURE_STOP_REMOVE(1, false, false, true);

        public final int cost;
        public final boolean success;
        public final boolean keepProcessing;
        public final boolean remove;

        WorkResult(int cost, boolean success, boolean keepProcessing, boolean remove) {
            this.cost = cost;
            this.success = success;
            this.keepProcessing = keepProcessing;
            this.remove = remove;
        }
    }
}