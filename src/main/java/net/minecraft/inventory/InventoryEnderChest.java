package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;

public class InventoryEnderChest extends InventoryBasic
{
    private TileEntityEnderChest associatedChest;

    private final EntityPlayer owner;

    public InventoryEnderChest(EntityPlayer owner)
    {
        super("container.enderchest", false, 27);
        this.owner = owner;
    }

    public void setChestTileEntity(TileEntityEnderChest chestTileEntity)
    {
        this.associatedChest = chestTileEntity;
    }

    public void loadInventoryFromNBT(NBTTagList p_70486_1_)
    {
        for (int i = 0; i < this.getSizeInventory(); ++i)
        {
            this.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        for (int k = 0; k < p_70486_1_.tagCount(); ++k)
        {
            NBTTagCompound nbttagcompound = p_70486_1_.getCompoundTagAt(k);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < this.getSizeInventory())
            {
                this.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
    }

    public NBTTagList saveInventoryToNBT()
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.getSizeInventory(); ++i)
        {
            ItemStack itemstack = this.getStackInSlot(i);

            if (!itemstack.isEmpty())
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                itemstack.writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return this.associatedChest != null && !this.associatedChest.canBeUsed(player) ? false : super.isUsableByPlayer(player);
    }

    public void openInventory(EntityPlayer player)
    {
        if (this.associatedChest != null)
        {
            this.associatedChest.openChest();
        }

        super.openInventory(player);
    }

    public void closeInventory(EntityPlayer player)
    {
        if (this.associatedChest != null)
        {
            this.associatedChest.closeChest();
        }

        super.closeInventory(player);
        this.associatedChest = null;
    }

    public InventoryHolder getBukkitOwner() {
        return owner.getBukkitEntity();
    }

    @Override
    public Location getLocation() {
        if (associatedChest == null) return owner.getBukkitEntity().getLocation(); // Paper - return Player location if there is no TE
        return new Location(this.associatedChest.getWorld().getWorld(), this.associatedChest.getPos().getX(), this.associatedChest.getPos().getY(), this.associatedChest.getPos().getZ());
    }

}