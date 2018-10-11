package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class EnchantmentHelper
{
    private static final ModifierDamage ENCHANTMENT_MODIFIER_DAMAGE = new ModifierDamage();
    private static final ModifierLiving ENCHANTMENT_MODIFIER_LIVING = new ModifierLiving();
    private static final HurtIterator ENCHANTMENT_ITERATOR_HURT = new HurtIterator();
    private static final DamageIterator ENCHANTMENT_ITERATOR_DAMAGE = new DamageIterator();

    public static int getEnchantmentLevel(Enchantment enchID, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return 0;
        }
        else
        {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
                int j = nbttagcompound.getShort("lvl");

                if (enchantment == enchID)
                {
                    return j;
                }
            }

            return 0;
        }
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack)
    {
        Map<Enchantment, Integer> map = Maps.<Enchantment, Integer>newLinkedHashMap();
        NBTTagList nbttaglist = stack.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
            int j = nbttagcompound.getShort("lvl");
            map.put(enchantment, Integer.valueOf(j));
        }

        return map;
    }

    public static void setEnchantments(Map<Enchantment, Integer> enchMap, ItemStack stack)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (Entry<Enchantment, Integer> entry : enchMap.entrySet())
        {
            Enchantment enchantment = entry.getKey();

            if (enchantment != null)
            {
                int i = ((Integer)entry.getValue()).intValue();
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setShort("id", (short)Enchantment.getEnchantmentID(enchantment));
                nbttagcompound.setShort("lvl", (short)i);
                nbttaglist.appendTag(nbttagcompound);

                if (stack.getItem() == Items.ENCHANTED_BOOK)
                {
                    ItemEnchantedBook.addEnchantment(stack, new EnchantmentData(enchantment, i));
                }
            }
        }

        if (nbttaglist.hasNoTags())
        {
            if (stack.hasTagCompound())
            {
                stack.getTagCompound().removeTag("ench");
            }
        }
        else if (stack.getItem() != Items.ENCHANTED_BOOK)
        {
            stack.setTagInfo("ench", nbttaglist);
        }
    }

    private static void applyEnchantmentModifier(IModifier modifier, ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                int j = nbttaglist.getCompoundTagAt(i).getShort("id");
                int k = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                if (Enchantment.getEnchantmentByID(j) != null)
                {
                    modifier.calculateModifier(Enchantment.getEnchantmentByID(j), k);
                }
            }
        }
    }

    private static void applyEnchantmentModifierArray(IModifier modifier, Iterable<ItemStack> stacks)
    {
        for (ItemStack itemstack : stacks)
        {
            applyEnchantmentModifier(modifier, itemstack);
        }
    }

    public static int getEnchantmentModifierDamage(Iterable<ItemStack> stacks, DamageSource source)
    {
        ENCHANTMENT_MODIFIER_DAMAGE.damageModifier = 0;
        ENCHANTMENT_MODIFIER_DAMAGE.source = source;
        applyEnchantmentModifierArray(ENCHANTMENT_MODIFIER_DAMAGE, stacks);
        ENCHANTMENT_MODIFIER_DAMAGE.source = null; //Forge Fix memory leaks: https://bugs.mojang.com/browse/MC-128547
        return ENCHANTMENT_MODIFIER_DAMAGE.damageModifier;
    }

    public static float getModifierForCreature(ItemStack stack, EnumCreatureAttribute creatureAttribute)
    {
        ENCHANTMENT_MODIFIER_LIVING.livingModifier = 0.0F;
        ENCHANTMENT_MODIFIER_LIVING.entityLiving = creatureAttribute;
        applyEnchantmentModifier(ENCHANTMENT_MODIFIER_LIVING, stack);
        return ENCHANTMENT_MODIFIER_LIVING.livingModifier;
    }

    public static float getSweepingDamageRatio(EntityLivingBase p_191527_0_)
    {
        int i = getMaxEnchantmentLevel(Enchantments.SWEEPING, p_191527_0_);
        return i > 0 ? EnchantmentSweepingEdge.getSweepingDamageRatio(i) : 0.0F;
    }

    public static void applyThornEnchantments(EntityLivingBase p_151384_0_, Entity p_151384_1_)
    {
        ENCHANTMENT_ITERATOR_HURT.attacker = p_151384_1_;
        ENCHANTMENT_ITERATOR_HURT.user = p_151384_0_;

        if (p_151384_0_ != null)
        {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getEquipmentAndArmor());
        }

        if (p_151384_1_ instanceof EntityPlayer)
        {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getHeldItemMainhand());
        }

        ENCHANTMENT_ITERATOR_HURT.attacker = null; //Forge Fix memory leaks: https://bugs.mojang.com/browse/MC-128547
        ENCHANTMENT_ITERATOR_HURT.user = null;
    }

    public static void applyArthropodEnchantments(EntityLivingBase p_151385_0_, Entity p_151385_1_)
    {
        ENCHANTMENT_ITERATOR_DAMAGE.user = p_151385_0_;
        ENCHANTMENT_ITERATOR_DAMAGE.target = p_151385_1_;

        if (p_151385_0_ != null)
        {
            applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getEquipmentAndArmor());
        }

        if (p_151385_0_ instanceof EntityPlayer)
        {
            applyEnchantmentModifier(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getHeldItemMainhand());
        }

        ENCHANTMENT_ITERATOR_DAMAGE.target = null; //Forge Fix memory leaks: https://bugs.mojang.com/browse/MC-128547
        ENCHANTMENT_ITERATOR_DAMAGE.user = null;
    }

    public static int getMaxEnchantmentLevel(Enchantment p_185284_0_, EntityLivingBase p_185284_1_)
    {
        Iterable<ItemStack> iterable = p_185284_0_.getEntityEquipment(p_185284_1_);

        if (iterable == null)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (ItemStack itemstack : iterable)
            {
                int j = getEnchantmentLevel(p_185284_0_, itemstack);

                if (j > i)
                {
                    i = j;
                }
            }

            return i;
        }
    }

    public static int getKnockbackModifier(EntityLivingBase player)
    {
        return getMaxEnchantmentLevel(Enchantments.KNOCKBACK, player);
    }

    public static int getFireAspectModifier(EntityLivingBase player)
    {
        return getMaxEnchantmentLevel(Enchantments.FIRE_ASPECT, player);
    }

    public static int getRespirationModifier(EntityLivingBase p_185292_0_)
    {
        return getMaxEnchantmentLevel(Enchantments.RESPIRATION, p_185292_0_);
    }

    public static int getDepthStriderModifier(EntityLivingBase p_185294_0_)
    {
        return getMaxEnchantmentLevel(Enchantments.DEPTH_STRIDER, p_185294_0_);
    }

    public static int getEfficiencyModifier(EntityLivingBase p_185293_0_)
    {
        return getMaxEnchantmentLevel(Enchantments.EFFICIENCY, p_185293_0_);
    }

    public static int getFishingLuckBonus(ItemStack p_191529_0_)
    {
        return getEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, p_191529_0_);
    }

    public static int getFishingSpeedBonus(ItemStack p_191528_0_)
    {
        return getEnchantmentLevel(Enchantments.LURE, p_191528_0_);
    }

    public static int getLootingModifier(EntityLivingBase p_185283_0_)
    {
        return getMaxEnchantmentLevel(Enchantments.LOOTING, p_185283_0_);
    }

    public static boolean getAquaAffinityModifier(EntityLivingBase p_185287_0_)
    {
        return getMaxEnchantmentLevel(Enchantments.AQUA_AFFINITY, p_185287_0_) > 0;
    }

    public static boolean hasFrostWalkerEnchantment(EntityLivingBase player)
    {
        return getMaxEnchantmentLevel(Enchantments.FROST_WALKER, player) > 0;
    }

    public static boolean hasBindingCurse(ItemStack p_190938_0_)
    {
        return getEnchantmentLevel(Enchantments.BINDING_CURSE, p_190938_0_) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack p_190939_0_)
    {
        return getEnchantmentLevel(Enchantments.VANISHING_CURSE, p_190939_0_) > 0;
    }

    public static ItemStack getEnchantedItem(Enchantment p_92099_0_, EntityLivingBase p_92099_1_)
    {
        List<ItemStack> list = p_92099_0_.getEntityEquipment(p_92099_1_);

        if (list.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            List<ItemStack> list1 = Lists.<ItemStack>newArrayList();

            for (ItemStack itemstack : list)
            {
                if (!itemstack.isEmpty() && getEnchantmentLevel(p_92099_0_, itemstack) > 0)
                {
                    list1.add(itemstack);
                }
            }

            return list1.isEmpty() ? ItemStack.EMPTY : (ItemStack)list1.get(p_92099_1_.getRNG().nextInt(list1.size()));
        }
    }

    public static int calcItemStackEnchantability(Random rand, int enchantNum, int power, ItemStack stack)
    {
        Item item = stack.getItem();
        int i = item.getItemEnchantability(stack);

        if (i <= 0)
        {
            return 0;
        }
        else
        {
            if (power > 15)
            {
                power = 15;
            }

            int j = rand.nextInt(8) + 1 + (power >> 1) + rand.nextInt(power + 1);

            if (enchantNum == 0)
            {
                return Math.max(j / 3, 1);
            }
            else
            {
                return enchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, power * 2);
            }
        }
    }

    public static ItemStack addRandomEnchantment(Random random, ItemStack stack, int level, boolean allowTreasure)
    {
        List<EnchantmentData> list = buildEnchantmentList(random, stack, level, allowTreasure);
        boolean flag = stack.getItem() == Items.BOOK;

        if (flag)
        {
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        for (EnchantmentData enchantmentdata : list)
        {
            if (flag)
            {
                ItemEnchantedBook.addEnchantment(stack, enchantmentdata);
            }
            else
            {
                stack.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
            }
        }

        return stack;
    }

    public static List<EnchantmentData> buildEnchantmentList(Random randomIn, ItemStack itemStackIn, int level, boolean allowTreasure)
    {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();
        Item item = itemStackIn.getItem();
        int i = item.getItemEnchantability(itemStackIn);

        if (i <= 0)
        {
            return list;
        }
        else
        {
            level = level + 1 + randomIn.nextInt(i / 4 + 1) + randomIn.nextInt(i / 4 + 1);
            float f = (randomIn.nextFloat() + randomIn.nextFloat() - 1.0F) * 0.15F;
            level = MathHelper.clamp(Math.round((float)level + (float)level * f), 1, Integer.MAX_VALUE);
            List<EnchantmentData> list1 = getEnchantmentDatas(level, itemStackIn, allowTreasure);

            if (!list1.isEmpty())
            {
                list.add(WeightedRandom.getRandomItem(randomIn, list1));

                while (randomIn.nextInt(50) <= level)
                {
                    removeIncompatible(list1, (EnchantmentData)Util.getLastElement(list));

                    if (list1.isEmpty())
                    {
                        break;
                    }

                    list.add(WeightedRandom.getRandomItem(randomIn, list1));
                    level /= 2;
                }
            }

            return list;
        }
    }

    public static void removeIncompatible(List<EnchantmentData> p_185282_0_, EnchantmentData p_185282_1_)
    {
        Iterator<EnchantmentData> iterator = p_185282_0_.iterator();

        while (iterator.hasNext())
        {
            if (!p_185282_1_.enchantment.isCompatibleWith((iterator.next()).enchantment))
            {
                iterator.remove();
            }
        }
    }

    public static List<EnchantmentData> getEnchantmentDatas(int p_185291_0_, ItemStack p_185291_1_, boolean allowTreasure)
    {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();
        Item item = p_185291_1_.getItem();
        boolean flag = p_185291_1_.getItem() == Items.BOOK;

        for (Enchantment enchantment : Enchantment.REGISTRY)
        {
            if ((!enchantment.isTreasureEnchantment() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(p_185291_1_) || (flag && enchantment.isAllowedOnBooks())))
            {
                for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i)
                {
                    if (p_185291_0_ >= enchantment.getMinEnchantability(i) && p_185291_0_ <= enchantment.getMaxEnchantability(i))
                    {
                        list.add(new EnchantmentData(enchantment, i));
                        break;
                    }
                }
            }
        }

        return list;
    }

    static final class DamageIterator implements IModifier
        {
            public EntityLivingBase user;
            public Entity target;

            private DamageIterator()
            {
            }

            public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
            {
                enchantmentIn.onEntityDamaged(this.user, this.target, enchantmentLevel);
            }
        }

    static final class HurtIterator implements IModifier
        {
            public EntityLivingBase user;
            public Entity attacker;

            private HurtIterator()
            {
            }

            public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
            {
                enchantmentIn.onUserHurt(this.user, this.attacker, enchantmentLevel);
            }
        }

    interface IModifier
    {
        void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel);
    }

    static final class ModifierDamage implements IModifier
        {
            public int damageModifier;
            public DamageSource source;

            private ModifierDamage()
            {
            }

            public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
            {
                this.damageModifier += enchantmentIn.calcModifierDamage(enchantmentLevel, this.source);
            }
        }

    static final class ModifierLiving implements IModifier
        {
            public float livingModifier;
            public EnumCreatureAttribute entityLiving;

            private ModifierLiving()
            {
            }

            public void calculateModifier(Enchantment enchantmentIn, int enchantmentLevel)
            {
                this.livingModifier += enchantmentIn.calcDamageByCreature(enchantmentLevel, this.entityLiving);
            }
        }
}