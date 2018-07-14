package grondag.render_hooks.core;

import grondag.render_hooks.api.IPipelinedQuad;
import grondag.render_hooks.api.IPipelinedQuadConsumer;
import grondag.render_hooks.api.PipelineManager;
import grondag.render_hooks.api.RenderPipeline;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompoundVertexLighter implements IPipelinedQuadConsumer
{
    protected final BlockInfo blockInfo;
    
    private PipelinedVertexLighter[] lighters = new PipelinedVertexLighter[PipelineManager.MAX_PIPELINES];

    private BlockRenderLayer renderLayer;
    private CompoundBufferBuilder target;
    private boolean didOutput;
    private boolean needsBlockInfoUpdate;
    private IBlockState blockState;
    private long positionRandom = Long.MIN_VALUE;
    private int sideFlags;
    
    private class ChildLighter extends PipelinedVertexLighter
    {
        protected ChildLighter(RenderPipeline pipeline)
        {
            super(pipeline);
        }

        @Override
        public final BlockInfo getBlockInfo()
        {
            if(needsBlockInfoUpdate)
            {
                blockInfo.updateShift();
                if(Minecraft.isAmbientOcclusionEnabled())
                    blockInfo.updateLightMatrix();
                else
                    blockInfo.updateFlatLighting();
            }
            return blockInfo;
        }
        
        @Override
        public final BufferBuilder getPipelineBuffer()
        {
            return target.getPipelineBuffer(this.pipeline);
        }
    }
    
    public void prepare(CompoundBufferBuilder target, BlockRenderLayer layer, IBlockAccess world, IBlockState blockState, BlockPos pos, boolean checkSides)
    {
        this.renderLayer = layer;
        this.target = target;
        this.didOutput = false;
        this.needsBlockInfoUpdate = true;
        this.positionRandom = Long.MIN_VALUE;
        this.blockInfo.setWorld(world);
        this.blockInfo.setState(blockState);
        this.blockInfo.setBlockPos(pos);
        this.blockState = blockState;
        this.sideFlags = checkSides ? getSideFlags() : 0xFFFF;
    }
    
    public void releaseResources()
    {
        this.blockInfo.reset();
    }
    
    private int getSideFlags()
    {
        int result = 0;
        for (EnumFacing face : EnumFacing.values())
        {
            if(this.blockState.shouldSideBeRendered(this.world(), this.pos(), face))
                result |= (1 << face.ordinal());
        }
        return result;
    }
    
    @Override
    public void accept(IPipelinedQuad quad)
    {
        if(quad.getRenderLayer() == this.renderLayer)
            getPipelineLighter(quad.getPipeline()).acceptQuad(quad);
    }
    
    public CompoundVertexLighter()
    {
        this.blockInfo = new BlockInfo(Minecraft.getMinecraft().getBlockColors());
    }

    private PipelinedVertexLighter getPipelineLighter(RenderPipeline pipeline)
    {
        PipelinedVertexLighter result = lighters[pipeline.getIndex()];
        if(result == null)
        {
            result = new ChildLighter(pipeline);
            lighters[pipeline.getIndex()] = result;
        }
        return result;
    }
    
    public boolean didOutput()
    {
        return this.didOutput;
    }
    
    public long getPositionRandom()
    {
        if(this.positionRandom == Long.MIN_VALUE)
        {
            this.positionRandom = MathHelper.getPositionRandom(this.pos());
        }
        return this.positionRandom;
    }

    @Override
    public boolean shouldOutputSide(EnumFacing side)
    {
        return (this.sideFlags & (1 << side.ordinal())) != 0;
    }

    @Override
    public BlockRenderLayer targetLayer()
    {
        return this.renderLayer;
    }

    @Override
    public final BlockPos pos()
    {
        return this.blockInfo.getBlockPos();
    }

    @Override
    public final IBlockAccess world()
    {
        return this.blockInfo.getWorld();
    }

    @Override
    public IBlockState blockState()
    {
        return this.blockState;
    }

    @Override
    public long positionRandom()
    {
        return this.positionRandom;
    }
}
