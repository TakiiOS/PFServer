package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class StringUtils
{
    private static final Pattern PATTERN_CONTROL_CODE = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

    @SideOnly(Side.CLIENT)
    public static String ticksToElapsedTime(int ticks)
    {
        int i = ticks / 20;
        int j = i / 60;
        i = i % 60;
        return i < 10 ? j + ":0" + i : j + ":" + i;
    }

    @SideOnly(Side.CLIENT)
    public static String stripControlCodes(String text)
    {
        return PATTERN_CONTROL_CODE.matcher(text).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String string)
    {
        return org.apache.commons.lang3.StringUtils.isEmpty(string);
    }
}