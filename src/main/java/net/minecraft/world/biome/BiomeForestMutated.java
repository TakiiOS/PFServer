package net.minecraft.world.biome;

import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class BiomeForestMutated extends BiomeForest
{
    public BiomeForestMutated(BiomeProperties properties)
    {
        super(Type.BIRCH, properties);
    }

    public WorldGenAbstractTree getRandomTreeFeature(Random rand)
    {
        return rand.nextBoolean() ? BiomeForest.SUPER_BIRCH_TREE : BiomeForest.BIRCH_TREE;
    }
}