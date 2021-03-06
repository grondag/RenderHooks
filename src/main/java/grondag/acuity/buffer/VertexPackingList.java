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

package grondag.acuity.buffer;

import java.util.function.Consumer;

import grondag.acuity.api.pipeline.RenderPipelineImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Tracks number of vertices, pipeline and sequence thereof within a buffer.
 */
@Environment(EnvType.CLIENT)
public class VertexPackingList
{
    private int[] counts = new int[16];
    private RenderPipelineImpl[] pipelines = new RenderPipelineImpl[16];
    
    private int size = 0;
    private int totalBytes = 0;
    
    public void clear()
    {
        this.size = 0;
        this.totalBytes = 0;
    }
    
    public int size()
    {
        return this.size;
    }
    
    /**
     * For performance testing.
     */
    public int quadCount()
    {
        if(this.size == 0) 
            return 0;
        
        int quads = 0;
        
        for(int i = 0; i < this.size; i++)
        {
            quads += this.counts[i] / 4;
        }
        return quads;
    }
    
    public int totalBytes()
    {
        return this.totalBytes;
    }
    
    public void addPacking(RenderPipelineImpl pipeline, int vertexCount)
    {
        if (size == this.pipelines.length)
        {
            final int iCopy[] = new int[size * 2];
            System.arraycopy(this.counts, 0, iCopy, 0, size);
            this.counts  = iCopy;
            
            final RenderPipelineImpl pCopy[] = new RenderPipelineImpl[size * 2];
            System.arraycopy(this.pipelines, 0, pCopy, 0, size);
            this.pipelines  = pCopy;
        }
        this.pipelines[size] = pipeline;
        this.counts[size] = vertexCount;
        this.totalBytes += pipeline.textureDepth.vertexFormat.stride * vertexCount;
        this.size++;
    }
    
    public static interface VertexPackingConsumer
    {
        void accept(RenderPipelineImpl pipeline, int vertexCount);
    }
    
    public final void forEach(VertexPackingConsumer consumer)
    {
        final int size = this.size;
        for(int i = 0; i < size; i++)
        {
            consumer.accept(this.pipelines[i], this.counts[i]);
        }
    }
    
    public final void forEachPipeline(Consumer<RenderPipelineImpl> consumer)
    {
        final int size = this.size;
        for(int i = 0; i < size; i++)
            consumer.accept(this.pipelines[i]);
    }
    
    public final RenderPipelineImpl getPipeline(int index)
    {
        return this.pipelines[index];
    }
    
    public final int getCount(int index)
    {
        return this.counts[index];
    }
}
