package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Predicate;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public interface ICondition
{
    ICondition TRUE = new ICondition()
    {
        public Predicate<IBlockState> getPredicate(BlockStateContainer blockState)
        {
            return new Predicate<IBlockState>()
            {
                public boolean apply(@Nullable IBlockState p_apply_1_)
                {
                    return true;
                }
            };
        }
    };
    ICondition FALSE = new ICondition()
    {
        public Predicate<IBlockState> getPredicate(BlockStateContainer blockState)
        {
            return new Predicate<IBlockState>()
            {
                public boolean apply(@Nullable IBlockState p_apply_1_)
                {
                    return false;
                }
            };
        }
    };

    Predicate<IBlockState> getPredicate(BlockStateContainer blockState);
}