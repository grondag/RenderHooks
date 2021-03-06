/*******************************************************************************
 * Copyright (C) 2018 grondag
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package grondag.acuity.broken;

import java.nio.ByteBuffer;

import grondag.acuity.api.pipeline.RenderPipeline;
import grondag.acuity.api.pipeline.RenderPipelineImpl;
import grondag.acuity.fermion.config.Localization;
import grondag.acuity.api.IPipelinedQuad;
import grondag.acuity.api.IPipelinedVertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;

/**
 * Similar in form and function to Forge's quad gathering transformer and child classes
 * but directly inherits from BufferBuilder.<p>
 * 
 * At time of writing, only supported use is for handling Minecraft native fluid rendering.
 * Could get fancier, but expect fluid rendering to change drastically in 1.13.
 * 
 */
@Environment(EnvType.CLIENT)
public class FluidBuilder extends BufferBuilder implements IPipelinedQuad
{
    private static final int SKY = 0;
    private static final int BLOCK = 1;
    
    private CompoundVertexLighter lighter;
    private RenderPipelineImpl pipeline;
    private int vertexCount = 0;
    private final Vector3f[] pos = new Vector3f[4];
    private final int[] color = new int[4];
    private final int[][] lightmap = new int[4][2];
    private final float[][] tex = new float[4][2];
    
    public FluidBuilder()
    {   
        // parent buffer is never used
        super(1);
        pos[0] = new Vector3f();
        pos[1] = new Vector3f();
        pos[2] = new Vector3f();
        pos[3] = new Vector3f();
    }

    public FluidBuilder prepare(RenderPipelineImpl pipeline, CompoundVertexLighter lighter)
    {
        this.pipeline = pipeline;
        this.lighter = lighter;
        this.vertexCount = 0;
        return this;
    }
    
    @Override
    public void sortVertexData(float p_181674_1_, float p_181674_2_, float p_181674_3_)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public State getVertexState()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void setVertexState(State state)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void reset()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void begin(int glMode, VertexFormat format)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public BufferBuilder tex(double u, double v)
    {
        float[] target = this.tex[this.vertexCount];
        target[0] = (float)u;
        target[1] = (float)v;
        return this;
    }

    @Override
    public BufferBuilder lightmap(int skyLight, int blockLight)
    {
        int[] target = this.lightmap[this.vertexCount];
        // convert from 0-15 (shifted << 4) to 0-255
        target[SKY] = (skyLight >> 4) * 17;
        target[BLOCK] = (blockLight >> 4) * 17;
        return this;
    }

    @Override
    public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putPosition(double x, double y, double z)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public int getColorIndex(int vertexIndex)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColorMultiplier(float red, float green, float blue, int vertexIndex)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColorRGB_F(float red, float green, float blue, int vertexIndex)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColorRGBA(int index, int red, int green, int blue)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void noColor()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public BufferBuilder color(float red, float green, float blue, float alpha)
    {
        this.color[this.vertexCount] = Math.round(red * 255) | (Math.round(green * 255) <<  8) | (Math.round(blue * 255) << 16) | (Math.round(alpha * 255) << 24);
        return this;
    }

    @Override
    public BufferBuilder color(int red, int green, int blue, int alpha)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void addVertexData(int[] vertexData)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void endVertex()
    {
        if(this.vertexCount == 3)
        {
            this.lighter.accept(this);
            this.vertexCount = 0;
        }
        else
            vertexCount++;
    }

    @Override
    public BufferBuilder pos(double x, double y, double z)
    {
        // fluid renderer is sending this to a buffer builder and thus already
        // adds the block offset which is why it passes as a double.
        // We are sending to the lighter, which will do that again and expects 
        // floats so reverse the change and store in a format appropriate for our use.
        final BlockPos blockPos = lighter.pos();
        
        this.pos[this.vertexCount].set((float)(x - blockPos.getX()), (float)(y - blockPos.getY()), (float)(z - blockPos.getZ()));
        return this;
    }

    @Override
    public void putNormal(float x, float y, float z)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public BufferBuilder normal(float x, float y, float z)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void setTranslation(double x, double y, double z)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void finishDrawing()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public VertexFormat getVertexFormat()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public int getVertexCount()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public int getDrawMode()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColor4(int argb)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColorRGB_F4(float red, float green, float blue)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putColorRGBA(int index, int red, int green, int blue, int alpha)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public boolean isColorDisabled()
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public void putBulkData(ByteBuffer buffer)
    {
        throw new UnsupportedOperationException(Localization.translate("misc.error_wrapped_buffer_unsupported"));
    }

    @Override
    public RenderPipeline getPipeline()
    {
        return this.pipeline;
    }

    @Override
    public void produceVertices(IPipelinedVertexConsumer vertexLighter)
    {
        vertexLighter.setAmbientOcclusion(false);
        vertexLighter.setShading(false);
        float normX, normY, normZ;
        
        // PERF: garbage factory
        Vector3f v1 = new Vector3f(pos[3]);
        Vector3f t = new Vector3f(pos[1]);  
        Vector3f v2 = new Vector3f(pos[2]);  
        v1.subtract(t);
        t.set(pos[0].x(), pos[0].y(), pos[0].z());
        v2.subtract(t);
        v1.cross(v2);
        v1.normalize();
        normX = v1.x();
        normY = v1.y();
        normZ = v1.z();
        
        for(int i = 0; i < 4; i++)
        {
            vertexLighter.setSkyLightMap(this.lightmap[i][SKY]);
            int blockLight = this.lightmap[i][BLOCK];
            vertexLighter.setBlockLightMap(blockLight, blockLight, blockLight, 0xFF);
            vertexLighter.acceptVertex(pos[i].x(), pos[i].y(), pos[i].z(), normX, normY, normZ, color[i], tex[i][0], tex[i][1]);
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return MinecraftForgeClient.getRenderLayer();
    }
    
}
