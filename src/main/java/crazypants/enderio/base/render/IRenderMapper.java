package crazypants.enderio.base.render;

import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import crazypants.enderio.base.render.property.IOMode.EnumIOMode;
import crazypants.enderio.base.render.util.ItemQuadCollector;
import crazypants.enderio.base.render.util.QuadCollector;

/**
 * A render mapper maps the state of a placed block or an item stack into something that can be rendered.
 */
public interface IRenderMapper {

    /**
     * Render mappers that can provide custom render data for blocks. If a block's render mapper doesn't implement this,
     * then the block will be not be rendered
     * unless it is painted.
     *
     * <p>
     * The model generated by this will be cached if the block initializes the state wrapper with a cache key.
     */
    public static interface IBlockRenderMapper extends IRenderMapper {

        /**
         * Get a list of blockstates to render for the given block. Optionally, this method can add quads to the given
         * quad collector directly.
         * <p>
         * May return null.
         * <p>
         * Note: This will be called once for the block's getBlockLayer() or once per render layer if the render mapper
         * is IRenderLayerAware.
         */
        @SideOnly(Side.CLIENT)
        @Nullable
        List<IBlockState> mapBlockRender(@Nonnull IBlockStateWrapper state, @Nonnull IBlockAccess world,
                                         @Nonnull BlockPos pos, BlockRenderLayer blockLayer,
                                         @Nonnull QuadCollector quadCollector);

        /**
         * Get a mapping of EnumFacing to EnumIOMode to render as overlay layer for the given block.
         * <p>
         * May return null.
         * <p>
         * Note: This will only be called once, with isPainted either true or false. The render layer is determined by
         * the IO block.
         */
        @SideOnly(Side.CLIENT)
        default @Nullable EnumMap<EnumFacing, EnumIOMode> mapOverlayLayer(@Nonnull IBlockStateWrapper state,
                                                                          @Nonnull IBlockAccess world,
                                                                          @Nonnull BlockPos pos,
                                                                          boolean isPainted) {
            return null;
        }

        /**
         * Render mappers that implement this sub-interface will be called each for block render layer. They are
         * expected to check for the current render layer and
         * decide what to return.
         * <p>
         * Without this interface, the mapper will only be called if the render layer matches the block's
         * getBlockLayer(). This allows painted blocks to simply
         * render in all layers without having to check layers themselves.
         */
        public static interface IRenderLayerAware extends IBlockRenderMapper {

            /**
             * Render mappers that implement this sub-interface will be called even if their block is painted.
             */
            public static interface IPaintAware extends IRenderLayerAware {

            }
        }
    }

    public static interface IItemRenderMapper extends IRenderMapper {

        /**
         * Render mappers that can provide custom render data for items in the form of block states. Those block states
         * will be used to look up baked models, which
         * will then be disected into baked quads, which will be cached by item+meta.
         */
        public static interface IItemStateMapper extends IItemRenderMapper {

            /**
             * Get lists of blockstates and their item stacks to render for the given item stack.
             * <p>
             * If the item stack of a pair is null and the block state does not belong to the same block as the render
             * mapper, an item stack for the block in the
             * block state will be generated, using the block's state to meta method. If it does, the original item
             * stack will be reused.
             * <p>
             * The given block is the block of the item in the stack. It is given to save the method the effort to get
             * it out of the stack when the caller already had
             * to do it.
             * <p>
             * May return null to render nothing. Will not be called for block-painted items.
             */
            @SideOnly(Side.CLIENT)
            @Nullable
            List<Pair<IBlockState, ItemStack>> mapItemRender(@Nonnull Block block, @Nonnull ItemStack stack,
                                                             @Nonnull ItemQuadCollector itemQuadCollector);
        }

        /**
         * Render mappers that can provide custom render data for items in the form of baked models. Those will be
         * disected into baked quads, which will be cached
         * by item+meta+cacheKey. If the returned cacheKey is null, the model will not be cached at all.
         */
        public static interface IItemModelMapper extends IItemRenderMapper {

            /**
             * Get lists of baked model to render for the given item stack.
             * <p>
             * The given block is the block of the item in the stack. It is given to save the method the effort to get
             * it out of the stack when the caller already had
             * to do it.
             * <p>
             * May return null to render nothing. Will not be called for block-painted items. If the returned cacheKey
             * is null, the model will not be cached.
             */
            @SideOnly(Side.CLIENT)
            @Nullable
            List<IBakedModel> mapItemRender(@Nonnull Block block, @Nonnull ItemStack stack);
        }

        /**
         * Render mappers that need to decorate their item render with dynamic data (e.g. RF bar on capBanks).
         */
        public static interface IDynamicOverlayMapper extends IItemRenderMapper {

            /**
             * Get lists of baked quads render for the given item stack in addition to the mapped block states. The
             * result of this method will not be cached, the
             * block states from mapItemRender() will be.
             * <p>
             * The given block is the block of the item in the stack. It is given to save the method the effort to get
             * it out of the stack when the caller already had
             * to do it.
             * <p>
             * May return null.
             */
            @SideOnly(Side.CLIENT)
            @Nullable
            ItemQuadCollector mapItemDynamicOverlayRender(@Nonnull Block block, @Nonnull ItemStack stack);
        }

        /**
         * Gets the cacheKey that should be used to cache the model. A cacheKey that was pre-populated with block+meta
         * is given as parameter. It this returns null,
         * no caching will be performed.
         */
        @SideOnly(Side.CLIENT)
        default @Nonnull ICacheKey getCacheKey(@Nonnull Block block, @Nonnull ItemStack stack,
                                               @Nonnull ICacheKey cacheKey) {
            return cacheKey;
        }
    }
}
