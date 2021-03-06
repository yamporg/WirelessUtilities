package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISidedTransferAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSidedTransferAugment extends ItemAugment {

    public ItemSidedTransferAugment() {
        super();
        setName("sided_transfer_augment");
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.sidedTransfer.energyMultiplier;
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.sidedTransfer.energyAddition;
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.sidedTransfer.energyDrain;
    }

    @Nullable
    @Override
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return Level.fromInt(ModConfig.augments.sidedTransfer.requiredLevel);
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.sidedTransfer.budgetAddition;
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return ModConfig.augments.sidedTransfer.budgetMultiplier;
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof ISidedTransferAugmentable )
            ((ISidedTransferAugmentable) augmentable).setSidedTransferAugmented(!ModConfig.augments.sidedTransfer.required || !stack.isEmpty());
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return ISidedTransferAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof ISidedTransferAugmentable;
    }
}
