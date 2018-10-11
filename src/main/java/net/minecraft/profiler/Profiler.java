package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Profiler
{
    public static final boolean ENABLED = Boolean.getBoolean("enableDebugMethodProfiler"); // CraftBukkit - disable unless specified in JVM arguments
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<String> sectionList = Lists.<String>newArrayList();
    private final List<Long> timestampList = Lists.<Long>newArrayList();
    // TODO: Should we use profilingEnabled instead of ENABLED?
    public boolean profilingEnabled;
    private String profilingSection = "";
    private final Map<String, Long> profilingMap = Maps.<String, Long>newHashMap();

    public void clearProfiling()
    {
        if (!ENABLED) return;
        this.profilingMap.clear();
        this.profilingSection = "";
        this.sectionList.clear();
    }

    public void startSection(String name)
    {
        if (!ENABLED) return;
        if (this.profilingEnabled)
        {
            if (!this.profilingSection.isEmpty())
            {
                this.profilingSection = this.profilingSection + ".";
            }

            this.profilingSection = this.profilingSection + name;
            this.sectionList.add(this.profilingSection);
            this.timestampList.add(Long.valueOf(System.nanoTime()));
        }
    }

    public void func_194340_a(Supplier<String> p_194340_1_)
    {
        if (!ENABLED) return;
        if (this.profilingEnabled)
        {
            this.startSection(p_194340_1_.get());
        }
    }

    public void endSection()
    {
        if (!ENABLED) return;
        if (this.profilingEnabled)
        {
            long i = System.nanoTime();
            long j = ((Long)this.timestampList.remove(this.timestampList.size() - 1)).longValue();
            this.sectionList.remove(this.sectionList.size() - 1);
            long k = i - j;

            if (this.profilingMap.containsKey(this.profilingSection))
            {
                this.profilingMap.put(this.profilingSection, Long.valueOf(((Long)this.profilingMap.get(this.profilingSection)).longValue() + k));
            }
            else
            {
                this.profilingMap.put(this.profilingSection, Long.valueOf(k));
            }

            if (k > 100000000L)
            {
                LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", this.profilingSection, Double.valueOf((double)k / 1000000.0D));
            }

            this.profilingSection = this.sectionList.isEmpty() ? "" : (String)this.sectionList.get(this.sectionList.size() - 1);
        }
    }

    public List<Result> getProfilingData(String profilerName)
    {
        if (!ENABLED || !this.profilingEnabled)
        {
            return Collections.<Result>emptyList();
        }
        else
        {
            long i = this.profilingMap.containsKey("root") ? ((Long)this.profilingMap.get("root")).longValue() : 0L;
            long j = this.profilingMap.containsKey(profilerName) ? ((Long)this.profilingMap.get(profilerName)).longValue() : -1L;
            List<Result> list = Lists.<Result>newArrayList();

            if (!profilerName.isEmpty())
            {
                profilerName = profilerName + ".";
            }

            long k = 0L;

            for (String s : this.profilingMap.keySet())
            {
                if (s.length() > profilerName.length() && s.startsWith(profilerName) && s.indexOf(".", profilerName.length() + 1) < 0)
                {
                    k += ((Long)this.profilingMap.get(s)).longValue();
                }
            }

            float f = (float)k;

            if (k < j)
            {
                k = j;
            }

            if (i < k)
            {
                i = k;
            }

            for (String s1 : this.profilingMap.keySet())
            {
                if (s1.length() > profilerName.length() && s1.startsWith(profilerName) && s1.indexOf(".", profilerName.length() + 1) < 0)
                {
                    long l = ((Long)this.profilingMap.get(s1)).longValue();
                    double d0 = (double)l * 100.0D / (double)k;
                    double d1 = (double)l * 100.0D / (double)i;
                    String s2 = s1.substring(profilerName.length());
                    list.add(new Result(s2, d0, d1));
                }
            }

            for (String s3 : this.profilingMap.keySet())
            {
                this.profilingMap.put(s3, Long.valueOf(((Long)this.profilingMap.get(s3)).longValue() * 999L / 1000L));
            }

            if ((float)k > f)
            {
                list.add(new Result("unspecified", (double)((float)k - f) * 100.0D / (double)k, (double)((float)k - f) * 100.0D / (double)i));
            }

            Collections.sort(list);
            list.add(0, new Result(profilerName, 100.0D, (double)k * 100.0D / (double)i));
            return list;
        }
    }

    public void endStartSection(String name)
    {
        if (!ENABLED) return;
        this.endSection();
        this.startSection(name);
    }

    public String getNameOfLastSection()
    {
        if (!ENABLED) return "[DISABLED]";
        return this.sectionList.isEmpty() ? "[UNKNOWN]" : (String)this.sectionList.get(this.sectionList.size() - 1);
    }

    @SideOnly(Side.CLIENT)
    public void func_194339_b(Supplier<String> p_194339_1_)
    {
        this.endSection();
        this.func_194340_a(p_194339_1_);
    }

    public static final class Result implements Comparable<Result>
        {
            public double usePercentage;
            public double totalUsePercentage;
            public String profilerName;

            public Result(String profilerName, double usePercentage, double totalUsePercentage)
            {
                this.profilerName = profilerName;
                this.usePercentage = usePercentage;
                this.totalUsePercentage = totalUsePercentage;
            }

            public int compareTo(Result p_compareTo_1_)
            {
                if (p_compareTo_1_.usePercentage < this.usePercentage)
                {
                    return -1;
                }
                else
                {
                    return p_compareTo_1_.usePercentage > this.usePercentage ? 1 : p_compareTo_1_.profilerName.compareTo(this.profilerName);
                }
            }

            @SideOnly(Side.CLIENT)
            public int getColor()
            {
                return (this.profilerName.hashCode() & 11184810) + 4473924;
            }
        }

    /**
     * Forge: Fix for MC-117087, World.updateEntities is wasting time calling Class.getSimpleName() when the profiler is not active
     */
    @Deprecated // TODO: remove (1.13)
    public void startSection(Class<?> profiledClass)
    {
        if (this.profilingEnabled)
        {
            startSection(profiledClass.getSimpleName());
        }
    }
}