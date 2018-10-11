package net.minecraft.scoreboard;

import com.google.common.collect.Maps;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public abstract class Team
{
    public boolean isSameTeam(@Nullable Team other)
    {
        if (other == null)
        {
            return false;
        }
        else
        {
            return this == other;
        }
    }

    public abstract String getName();

    public abstract String formatString(String input);

    @SideOnly(Side.CLIENT)
    public abstract boolean getSeeFriendlyInvisiblesEnabled();

    public abstract boolean getAllowFriendlyFire();

    @SideOnly(Side.CLIENT)
    public abstract Team.EnumVisible getNameTagVisibility();

    public abstract TextFormatting getColor();

    public abstract Collection<String> getMembershipCollection();

    public abstract EnumVisible getDeathMessageVisibility();

    public abstract CollisionRule getCollisionRule();

    public static enum CollisionRule
    {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("pushOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("pushOwnTeam", 3);

        private static final Map<String, CollisionRule> nameMap = Maps.<String, CollisionRule>newHashMap();
        public final String name;
        public final int id;

        public static String[] getNames()
        {
            return (String[])nameMap.keySet().toArray(new String[nameMap.size()]);
        }

        @Nullable
        public static Team.CollisionRule getByName(String nameIn)
        {
            return nameMap.get(nameIn);
        }

        private CollisionRule(String nameIn, int idIn)
        {
            this.name = nameIn;
            this.id = idIn;
        }

        static
        {
            for (CollisionRule team$collisionrule : values())
            {
                nameMap.put(team$collisionrule.name, team$collisionrule);
            }
        }
    }

    public static enum EnumVisible
    {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        private static final Map<String, EnumVisible> nameMap = Maps.<String, EnumVisible>newHashMap();
        public final String internalName;
        public final int id;

        public static String[] getNames()
        {
            return (String[])nameMap.keySet().toArray(new String[nameMap.size()]);
        }

        @Nullable
        public static Team.EnumVisible getByName(String nameIn)
        {
            return nameMap.get(nameIn);
        }

        private EnumVisible(String nameIn, int idIn)
        {
            this.internalName = nameIn;
            this.id = idIn;
        }

        static
        {
            for (EnumVisible team$enumvisible : values())
            {
                nameMap.put(team$enumvisible.internalName, team$enumvisible);
            }
        }
    }
}