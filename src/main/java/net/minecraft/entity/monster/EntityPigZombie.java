package net.minecraft.entity.monster;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityPigZombie extends EntityZombie
{
    private static final UUID ATTACK_SPEED_BOOST_MODIFIER_UUID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private static final AttributeModifier ATTACK_SPEED_BOOST_MODIFIER = (new AttributeModifier(ATTACK_SPEED_BOOST_MODIFIER_UUID, "Attacking speed boost", 0.05D, 0)).setSaved(false);
    public int angerLevel;
    private int randomSoundDelay;
    private UUID angerTargetUUID;

    public EntityPigZombie(World worldIn)
    {
        super(worldIn);
        this.isImmuneToFire = true;
    }

    public void setRevengeTarget(@Nullable EntityLivingBase livingBase)
    {
        super.setRevengeTarget(livingBase);

        if (livingBase != null)
        {
            this.angerTargetUUID = livingBase.getUniqueID();
        }
    }

    protected void applyEntityAI()
    {
        this.targetTasks.addTask(1, new AIHurtByAggressor(this));
        this.targetTasks.addTask(2, new AITargetAggressor(this));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
    }

    protected void updateAITasks()
    {
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (this.isAngry())
        {
            if (!this.isChild() && !iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER))
            {
                iattributeinstance.applyModifier(ATTACK_SPEED_BOOST_MODIFIER);
            }

            --this.angerLevel;
        }
        else if (iattributeinstance.hasModifier(ATTACK_SPEED_BOOST_MODIFIER))
        {
            iattributeinstance.removeModifier(ATTACK_SPEED_BOOST_MODIFIER);
        }

        if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0)
        {
            this.playSound(SoundEvents.ENTITY_ZOMBIE_PIG_ANGRY, this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
        }

        if (this.angerLevel > 0 && this.angerTargetUUID != null && this.getRevengeTarget() == null)
        {
            EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
            this.setRevengeTarget(entityplayer);
            this.attackingPlayer = entityplayer;
            this.recentlyHit = this.getRevengeTimer();
        }

        super.updateAITasks();
    }

    public boolean getCanSpawnHere()
    {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
    }

    public boolean isNotColliding()
    {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    public static void registerFixesPigZombie(DataFixer fixer)
    {
        EntityLiving.registerFixesMob(fixer, EntityPigZombie.class);
    }

    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setShort("Anger", (short)this.angerLevel);

        if (this.angerTargetUUID != null)
        {
            compound.setString("HurtBy", this.angerTargetUUID.toString());
        }
        else
        {
            compound.setString("HurtBy", "");
        }
    }

    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);
        this.angerLevel = compound.getShort("Anger");
        String s = compound.getString("HurtBy");

        if (!s.isEmpty())
        {
            this.angerTargetUUID = UUID.fromString(s);
            EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
            this.setRevengeTarget(entityplayer);

            if (entityplayer != null)
            {
                this.attackingPlayer = entityplayer;
                this.recentlyHit = this.getRevengeTimer();
            }
        }
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            Entity entity = source.getTrueSource();

            if (entity instanceof EntityPlayer)
            {
                this.becomeAngryAt(entity);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    private void becomeAngryAt(Entity p_70835_1_)
    {
        this.angerLevel = 400 + this.rand.nextInt(400);
        this.randomSoundDelay = this.rand.nextInt(40);

        if (p_70835_1_ instanceof EntityLivingBase)
        {
            this.setRevengeTarget((EntityLivingBase)p_70835_1_);
        }
    }

    public boolean isAngry()
    {
        return this.angerLevel > 0;
    }

    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_ZOMBIE_PIG_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_ZOMBIE_PIG_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_ZOMBIE_PIG_DEATH;
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_ZOMBIE_PIGMAN;
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return false;
    }

    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    protected ItemStack getSkullDrop()
    {
        return ItemStack.EMPTY;
    }

    public boolean isPreventingPlayerRest(EntityPlayer playerIn)
    {
        return this.isAngry();
    }

    static class AIHurtByAggressor extends EntityAIHurtByTarget
        {
            public AIHurtByAggressor(EntityPigZombie p_i45828_1_)
            {
                super(p_i45828_1_, true);
            }

            protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn)
            {
                super.setEntityAttackTarget(creatureIn, entityLivingBaseIn);

                if (creatureIn instanceof EntityPigZombie)
                {
                    ((EntityPigZombie)creatureIn).becomeAngryAt(entityLivingBaseIn);
                }
            }
        }

    static class AITargetAggressor extends EntityAINearestAttackableTarget<EntityPlayer>
        {
            public AITargetAggressor(EntityPigZombie p_i45829_1_)
            {
                super(p_i45829_1_, EntityPlayer.class, true);
            }

            public boolean shouldExecute()
            {
                return ((EntityPigZombie)this.taskOwner).isAngry() && super.shouldExecute();
            }
        }
}