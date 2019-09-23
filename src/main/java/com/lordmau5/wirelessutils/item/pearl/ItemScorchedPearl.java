package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.entity.pearl.EntityScorchedPearl;
import com.lordmau5.wirelessutils.item.base.IDimensionallyStableItem;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemScorchedPearl extends ItemBasePearl implements IDimensionallyStableItem {

    public ItemScorchedPearl() {
        super();
        setName("scorched_pearl");
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entity) {
        if ( entity instanceof EntityItemEnhanced )
            ((EntityItemEnhanced) entity).setBurnWhenImmune(false);

        if ( !entity.isInWater() )
            entity.setFire(1);
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        if ( ModConfig.items.scorchedPearl.fireUpPlayers && entityIn instanceof EntityPlayer )
            entityIn.setFire(1);
    }

    @Override
    public boolean allowDimensionalTravel() {
        return !ModConfig.items.voidPearl.enableVoiding;
    }

    @Override
    public void onPortalImpact(@Nonnull ItemStack stack, @Nonnull EntityItem entity, @Nonnull IBlockState state) {
        if ( !entity.world.isRemote && ModConfig.items.voidPearl.enableVoiding ) {
            stack = new ItemStack(ModItems.itemVoidPearl, 1);
            entity.setItem(stack);

            if ( entity.world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) entity.world;
                ws.spawnParticle(EnumParticleTypes.PORTAL, false, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
                entity.world.playSound(null, entity.getPosition(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.NEUTRAL, 1F, 0.2F);
            }

            String thrower = entity.getThrower();
            EntityPlayer player = thrower == null ? null : entity.world.getPlayerEntityByName(thrower);
            if ( player instanceof EntityPlayerMP )
                ModAdvancements.THE_VOID_TOLLS.trigger((EntityPlayerMP) player);
        }

        ModItems.itemVoidPearl.onPortalImpact(stack, entity, state);
    }

    @Nonnull
    @Override
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityScorchedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityScorchedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityScorchedPearl(worldIn, stack);
    }
}
