package grondag.render_hooks.core;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionRenderCacheBuilder
{
    private final BufferBuilder[] worldRenderers = new BufferBuilder[BlockRenderLayer.values().length];
    public final RenderMaterialBufferManager materialBuffers = new RenderMaterialBufferManager();
    
    public RegionRenderCacheBuilder()
    {
        this.worldRenderers[BlockRenderLayer.SOLID.ordinal()] = new BufferBuilder(2097152);
        this.worldRenderers[BlockRenderLayer.CUTOUT.ordinal()] = new BufferBuilder(131072);
        this.worldRenderers[BlockRenderLayer.CUTOUT_MIPPED.ordinal()] = new BufferBuilder(131072);
        this.worldRenderers[BlockRenderLayer.TRANSLUCENT.ordinal()] = new BufferBuilder(262144);
    }

    public BufferBuilder getWorldRendererByLayer(BlockRenderLayer layer)
    {
        return this.worldRenderers[layer.ordinal()];
    }

    public BufferBuilder getWorldRendererByLayerId(int id)
    {
        return this.worldRenderers[id];
    }
}