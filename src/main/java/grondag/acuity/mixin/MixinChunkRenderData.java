package grondag.acuity.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import grondag.acuity.hooks.ChunkRebuildHelper;
import grondag.acuity.mixin.extension.ChunkRenderDataExt;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkVisibility;

@Mixin(ChunkRenderData.class)
public abstract class MixinChunkRenderData implements ChunkRenderDataExt
{
    @Shadow private boolean[] hasContent;
    @Shadow private boolean[] isInitialized;
    @Shadow private boolean empty;
    @Shadow private List<BlockEntity> blockEntities;
    @Shadow private ChunkVisibility chunkVisibility;
    @Shadow private BufferBuilder.State bufferState;
    
    @Override
    @Shadow public abstract void setNonEmpty(BlockRenderLayer blockRenderLayer);
    
    @Override
    public void clear()
    {
        empty = true;
        System.arraycopy(ChunkRebuildHelper.EMPTY_RENDER_LAYER_FLAGS, 0, hasContent, 0, ChunkRebuildHelper.BLOCK_RENDER_LAYER_COUNT);
        System.arraycopy(ChunkRebuildHelper.EMPTY_RENDER_LAYER_FLAGS, 0, isInitialized, 0, ChunkRebuildHelper.BLOCK_RENDER_LAYER_COUNT);
        chunkVisibility.setAll(false);
        bufferState = null;
        blockEntities.clear();
    }
    
}
