package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;
import java.util.Random;

public class TileEntityDispenser extends TileEntityLockableLoot
{
    private static final Random RNG = new Random();
    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(9, ItemStack.EMPTY);
    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;
    
    public List<ItemStack> getContents() {
        return this.stacks;
    }
        
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }
    
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }
        
    public List<HumanEntity> getViewers() {
        return transaction;
    }
        
    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end
    public int getSizeInventory()
    {
        return 9;
    }

    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.stacks)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public int getDispenseSlot()
    {
        this.fillWithLoot((EntityPlayer)null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.stacks.size(); ++k)
        {
            if (!((ItemStack)this.stacks.get(k)).isEmpty() && RNG.nextInt(j++) == 0)
            {
                i = k;
            }
        }

        return i;
    }

    public int addItemStack(ItemStack stack)
    {
        for (int i = 0; i < this.stacks.size(); ++i)
        {
            if (((ItemStack)this.stacks.get(i)).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return i;
            }
        }

        return -1;
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.dispenser";
    }

    public static void registerFixes(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityDispenser.class, new String[] {"Items"}));
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.stacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        if (!this.checkLootAndRead(compound))
        {
            ItemStackHelper.loadAllItems(compound, this.stacks);
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }

        return compound;
    }

    public int getInventoryStackLimit()
    {
        return maxStack;
    }

    public String getGuiID()
    {
        return "minecraft:dispenser";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        this.fillWithLoot(playerIn);
        return new ContainerDispenser(playerInventory, this);
    }

    protected NonNullList<ItemStack> getItems()
    {
        return this.stacks;
    }
}