package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.item.BehaviorProjectileAccurateDispense;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBasePearl extends ItemBase implements IEnhancedItem, IDamageableItem {

    public ItemBasePearl() {
        super();

        setMaxStackSize(16);
        setMaxDamage(0);
        setHasSubtypes(true);

        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new BehaviorProjectileAccurateDispense() {
            @Override
            public float getProjectileInaccuracy(ItemStack stack) {
                return ItemBasePearl.this.getProjectileInaccuracy(stack);
            }

            @Override
            public float getProjectileVelocity(ItemStack stack) {
                return ItemBasePearl.this.getProjectileVelocity(stack);
            }

            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stack) {
                return ItemBasePearl.this.getProjectileEntity(worldIn, null, position, stack);
            }
        });
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(getRegistryName() + "_stabilized", "inventory"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean shouldItemTakeDamage(EntityItemEnhanced entity, ItemStack stack, DamageSource source, float amount) {
        if ( source == DamageSource.IN_FIRE || source == DamageSource.LIGHTNING_BOLT )
            return false;

        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);

        if ( stack.getMetadata() == 1 )
            return new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".accurate_pearl.name",
                    name
            ).getUnformattedText();

        return name;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if ( stack.getMetadata() == 1 )
            addLocalizedLines(
                    tooltip,
                    "item." + WirelessUtils.MODID + ".accurate_pearl.info",
                    TextHelpers.GREEN
            );

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public float getProjectileVelocity(@Nonnull ItemStack stack) {
        return 1.5F;
    }

    public float getProjectileInaccuracy(@Nonnull ItemStack stack) {
        if ( stack.getMetadata() == 1 )
            return 0F;

        return 2F;
    }

    public abstract @Nonnull
    EntityThrowable getProjectileEntity(@Nonnull World worldIn, EntityPlayer playerIn, IPosition position, @Nonnull ItemStack stack);

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if ( !playerIn.capabilities.isCreativeMode )
            stack.shrink(1);

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldownTracker().setCooldown(this, 5);

        if ( !worldIn.isRemote ) {
            EntityThrowable pearl = getProjectileEntity(worldIn, playerIn, null, stack);
            pearl.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, getProjectileVelocity(stack), getProjectileInaccuracy(stack));
            worldIn.spawnEntity(pearl);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}