package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerHurtEntityTrigger implements ICriterionTrigger<PlayerHurtEntityTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, Listeners>newHashMap();

    public ResourceLocation getId()
    {
        return ID;
    }

    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners playerhurtentitytrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (playerhurtentitytrigger$listeners == null)
        {
            playerhurtentitytrigger$listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, playerhurtentitytrigger$listeners);
        }

        playerhurtentitytrigger$listeners.add(listener);
    }

    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners playerhurtentitytrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (playerhurtentitytrigger$listeners != null)
        {
            playerhurtentitytrigger$listeners.remove(listener);

            if (playerhurtentitytrigger$listeners.isEmpty())
            {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        DamagePredicate damagepredicate = DamagePredicate.deserialize(json.get("damage"));
        EntityPredicate entitypredicate = EntityPredicate.deserialize(json.get("entity"));
        return new Instance(damagepredicate, entitypredicate);
    }

    public void trigger(EntityPlayerMP player, Entity entityIn, DamageSource source, float amountDealt, float amountTaken, boolean blocked)
    {
        Listeners playerhurtentitytrigger$listeners = this.listeners.get(player.getAdvancements());

        if (playerhurtentitytrigger$listeners != null)
        {
            playerhurtentitytrigger$listeners.trigger(player, entityIn, source, amountDealt, amountTaken, blocked);
        }
    }

    public static class Instance extends AbstractCriterionInstance
        {
            private final DamagePredicate damage;
            private final EntityPredicate entity;

            public Instance(DamagePredicate damage, EntityPredicate entity)
            {
                super(PlayerHurtEntityTrigger.ID);
                this.damage = damage;
                this.entity = entity;
            }

            public boolean test(EntityPlayerMP player, Entity entity, DamageSource source, float dealt, float taken, boolean blocked)
            {
                if (!this.damage.test(player, source, dealt, taken, blocked))
                {
                    return false;
                }
                else
                {
                    return this.entity.test(player, entity);
                }
            }
        }

    static class Listeners
        {
            private final PlayerAdvancements playerAdvancements;
            private final Set<ICriterionTrigger.Listener<Instance>> listeners = Sets.<ICriterionTrigger.Listener<Instance>>newHashSet();

            public Listeners(PlayerAdvancements playerAdvancementsIn)
            {
                this.playerAdvancements = playerAdvancementsIn;
            }

            public boolean isEmpty()
            {
                return this.listeners.isEmpty();
            }

            public void add(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.add(listener);
            }

            public void remove(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.remove(listener);
            }

            public void trigger(EntityPlayerMP player, Entity entity, DamageSource source, float dealt, float taken, boolean blocked)
            {
                List<ICriterionTrigger.Listener<Instance>> list = null;

                for (ICriterionTrigger.Listener<Instance> listener : this.listeners)
                {
                    if (((Instance)listener.getCriterionInstance()).test(player, entity, source, dealt, taken, blocked))
                    {
                        if (list == null)
                        {
                            list = Lists.<ICriterionTrigger.Listener<Instance>>newArrayList();
                        }

                        list.add(listener);
                    }
                }

                if (list != null)
                {
                    for (ICriterionTrigger.Listener<Instance> listener1 : list)
                    {
                        listener1.grantCriterion(this.playerAdvancements);
                    }
                }
            }
        }
}