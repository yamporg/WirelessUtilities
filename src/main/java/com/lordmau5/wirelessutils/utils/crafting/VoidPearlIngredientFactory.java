package com.lordmau5.wirelessutils.utils.crafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

@SuppressWarnings("unused")
public class VoidPearlIngredientFactory implements IIngredientFactory {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Nonnull
    @Override
    public Ingredient parse(JsonContext context, JsonObject json) {
        if ( JsonUtils.hasField(json, "entity") ) {
            JsonElement entity = json.get("entity");
            String[] entityIds;

            if ( entity.isJsonArray() ) {
                JsonArray array = entity.getAsJsonArray();
                entityIds = new String[array.size()];
                for (int i = 0; i < entityIds.length; i++)
                    entityIds[i] = array.get(i).getAsString();

            } else
                entityIds = new String[]{entity.getAsString()};

            ItemStack[] stacks = new ItemStack[entityIds.length];
            int i = 0;
            int count = JsonUtils.getInt(json, "count", 1);

            for (String entityName : entityIds) {
                final ResourceLocation entityId = new ResourceLocation(entityName);
                if ( !EntityList.isRegistered(entityId) || EntityUtilities.isBlacklisted(entityId) )
                    continue;

                Class<? extends Entity> klass = EntityList.getClass(entityId);
                if ( klass == null )
                    continue;

                stacks[i] = new ItemStack(ModItems.itemVoidPearl, count, EntityList.getID(klass));
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("EntityID", entityName);

                if ( i == 0 && JsonUtils.hasField(json, "data") ) {
                    try {
                        JsonElement element = json.get("data");
                        NBTTagCompound data;
                        if ( element.isJsonObject() )
                            data = JsonToNBT.getTagFromJson(GSON.toJson(element));
                        else
                            data = JsonToNBT.getTagFromJson(element.getAsString());

                        tag.setTag("EntityData", data);

                    } catch (NBTException e) {
                        throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
                    }
                }

                stacks[i].setTagCompound(tag);
                i++;
            }

            if ( i == 0 )
                throw new JsonSyntaxException("No valid entities: " + Arrays.toString(entityIds));

            ItemStack[] out = new ItemStack[i];
            System.arraycopy(stacks, 0, out, 0, i);
            return new IngredientVoidPearl(out);
        }

        ItemStack stack = new ItemStack(ModItems.itemVoidPearl, 1);
        return new IngredientVoidPearl(stack);
    }

    public static class IngredientVoidPearl extends Ingredient {

        private final String[] entityIDs;
        private final NBTTagCompound entityData;

        public IngredientVoidPearl(ItemStack... stacks) {
            super(stacks);

            String[] ids = new String[stacks.length];
            int i = 0;

            NBTTagCompound data = null;

            for (ItemStack stack : stacks) {
                NBTTagCompound tag = stack.getTagCompound();
                if ( tag == null )
                    continue;

                if ( tag.hasKey("EntityID") ) {
                    ids[i] = tag.getString("EntityID");
                    i++;
                }

                if ( tag.hasKey("EntityData") )
                    data = tag.getCompoundTag("EntityData");
            }

            if ( i == 0 )
                entityIDs = null;
            else {
                entityIDs = new String[i];
                System.arraycopy(ids, 0, entityIDs, 0, i);
            }

            entityData = data;
        }

        @Override
        public boolean apply(@Nullable ItemStack input) {
            if ( input == null )
                return false;

            if ( input.getItem() != ModItems.itemVoidPearl )
                return false;

            NBTTagCompound inputTag = input.getTagCompound();
            final String inputEntity = inputTag == null ? null : inputTag.getString("EntityID");

            if ( entityIDs == null )
                return inputEntity == null;

            boolean match = false;
            for (String entityID : entityIDs) {
                if ( entityID.equalsIgnoreCase(inputEntity) ) {
                    match = true;
                    break;
                }
            }

            if ( !match )
                return false;

            if ( entityData != null ) {
                NBTTagCompound inputData = inputTag.getCompoundTag("EntityData");
                if ( inputData == null || inputData.getSize() == 0 )
                    return false;

                for (String key : entityData.getKeySet()) {
                    if ( !inputData.hasKey(key) || !entityData.getTag(key).equals(inputData.getTag(key)) )
                        return false;
                }
            }

            return true;
        }

        @Override
        public boolean isSimple() {
            return false;
        }
    }
}
