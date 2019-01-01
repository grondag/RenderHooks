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

package grondag.acuity.chunkbuild;

import java.util.concurrent.ArrayBlockingQueue;

import grondag.acuity.mixin.extension.ChunkRenderDataExt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkRenderData;

@Environment(EnvType.CLIENT)
public class ChunkRenderDataStore
{
    private static final ArrayBlockingQueue<ChunkRenderData> chunkDataPool = new ArrayBlockingQueue<>(4096);
 
    public static ChunkRenderData claim()
    {
        ChunkRenderData result = chunkDataPool.poll();
        if(result == null)
            result = new ChunkRenderData();
        
        return result;
    }
    
    public static void release(ChunkRenderData chunkRenderData)
    {
        ((ChunkRenderDataExt)chunkRenderData).clear();
        chunkDataPool.offer(chunkRenderData);
    }
}