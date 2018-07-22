package grondag.render_hooks.core;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.math.MathHelper;

/**
 * Holds a thread-safe cache of buffer builders to be used for VBO uploads
 */
public class BufferStore
{
    private static final ConcurrentLinkedQueue<ExpandableByteBuffer> store = new ConcurrentLinkedQueue<ExpandableByteBuffer>();
    private static final int BUFFER_SIZE_INCREMENT = 0x200000;
    
    public static class ExpandableByteBuffer
    {
        private ByteBuffer byteBuffer;
        private IntBuffer intBuffer;
        
        private ExpandableByteBuffer()
        {
            byteBuffer = GLAllocation.createDirectByteBuffer(BUFFER_SIZE_INCREMENT);
            intBuffer = byteBuffer.asIntBuffer();
        }
        
        public ByteBuffer byteBuffer()
        {
            return byteBuffer;
        }
        
        public IntBuffer intBuffer()
        {
            return intBuffer;
        }
        
        public void expand(int minByteSize)
        {
            if (minByteSize > this.byteBuffer.capacity())
            {
                ByteBuffer newBuffer = GLAllocation.createDirectByteBuffer(MathHelper.roundUp(minByteSize, BUFFER_SIZE_INCREMENT));
                int oldIntPos = this.intBuffer.position();
                int oldBytePos = this.byteBuffer.position();
                this.byteBuffer.position(0);
                newBuffer.put(this.byteBuffer);
                newBuffer.rewind();
                this.byteBuffer = newBuffer;
                this.intBuffer = newBuffer.asIntBuffer();
                newBuffer.position(oldBytePos);
                this.intBuffer.position(oldIntPos);
            }
        }
       
    }
    
    public static ExpandableByteBuffer claim()
    {
        ExpandableByteBuffer result =  store.poll();
        return result == null ? new ExpandableByteBuffer() : result;
    }
    
    public static void release(ExpandableByteBuffer buffer)
    {
        buffer.byteBuffer.clear();
        store.offer(buffer);
    }
}