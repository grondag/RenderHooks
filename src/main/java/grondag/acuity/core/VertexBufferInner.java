package grondag.acuity.core;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.APPLEFence;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import grondag.acuity.Configurator;
import grondag.acuity.api.RenderPipeline;
import grondag.acuity.api.TextureFormat;
import grondag.acuity.core.BufferStore.ExpandableByteBuffer;
import grondag.acuity.core.VertexPackingList.VertexPackingConsumer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class VertexBufferInner implements VertexPackingConsumer
{
    private static final ConcurrentLinkedQueue<VertexBufferInner> store = new ConcurrentLinkedQueue<VertexBufferInner>();
    
    public static VertexBufferInner claim()
    {
        VertexBufferInner result =  store.poll();
        
        if(result == null)
            result = new VertexBufferInner();
        else
            result.isNew = true;
        
        return result;
    }
    
    public static void release(VertexBufferInner buffer)
    {
        store.offer(buffer);
    }
    
    int bufferOffset = 0;
    int vertexOffset = 0;
    boolean isSolidLayer;
    
    // TODO: remove or improve
    boolean isNew = true;
    
    @Nullable PipelineVertexFormat lastFormat = null;
    
    /**
     * Holds VAO buffer names.  Null if VAO not available.
     */
    @Nullable IntBuffer vaoNames = null;
    
    /**
     * Contents of {@link #vaoNames} as java array - faster access.
     * Int buffer is retained for ease of teardown.
     */
    @Nullable int[] vaoBufferId = null;
    
    /**
     * Bit flags to indicate if VAO for texture format is setup.
     * Reset to 0 when buffer is uploaded.
     */
    int vaoBindingFlags = 0;
    
    protected int glBufferId;
    private VertexPackingList vertexPackingList;
    private int glFenceId;
    private boolean awaitingFence = false;
    private @Nullable ExpandableByteBuffer loadingBuffer;
    
    private VertexBufferInner()
    {
        this.vertexPackingList = new VertexPackingList();
        this.glFenceId = APPLEFence.glGenFencesAPPLE();
        this.glBufferId = OpenGlHelper.glGenBuffers();
        
        if(OpenGlHelperExt.isVaoEnabled()) try
        {
            IntBuffer vao = BufferUtils.createIntBuffer(TextureFormat.values().length);
            OpenGlHelperExt.glGenVertexArrays(vao);
            this.vaoNames = vao;
            int[] vaoBufferId = new int[TextureFormat.values().length];
            
            OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, glBufferId);

            for(TextureFormat format : TextureFormat.values())
            {
                final int bufferId = vao.get(format.ordinal());
                vaoBufferId[format.ordinal()] = bufferId;
                
                // can set up everything except binding offsets for 2nd and 3rd pipeline
                OpenGlHelperExt.glBindVertexArray(bufferId);
                
                GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                
                PipelineVertexFormat pvf = Configurator.lightingModel.vertexFormat(format);
                OpenGlHelperExt.enableAttributesVao(pvf.attributeCount);
                final int stride = pvf.stride; 
                final int bufferOffset = 0;
                OpenGlHelperExt.glVertexPointerFast(3, VertexFormatElement.EnumType.FLOAT.getGlConstant(), stride, bufferOffset);
                
                // UGLY: will wastefully bind secondary/tertiary layers
                pvf.bindAttributeLocations(bufferOffset);
                
                //TODO: leave the base attributes interleaved and pack extended attributes at the end
                //this will mean only the extended attributes ever have to be rebound
                //but may complicate vertex offset tracking
            }
            
            OpenGlHelperExt.glBindVertexArray(0);
            OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            this.vaoBufferId = vaoBufferId;
        }
        catch(Exception e)
        {
            // noop
        }
    }
    
    public VertexPackingList packingList()
    {
        return this.vertexPackingList;
    }
    
    @SuppressWarnings("null")
    public boolean isReady()
    {
        if(awaitingFence && APPLEFence.glTestFenceAPPLE(glFenceId))
        {
                awaitingFence = false;
                BufferStore.release(this.loadingBuffer);
                loadingBuffer = null;
                isNew = false;
        }
        return !(isNew || awaitingFence);
    }
    
    public final void upload(ExpandableByteBuffer uploadBuffer, VertexPackingList packing)
    {
        assert this.isNew;
        assert !this.awaitingFence; 
        assert this.loadingBuffer == null;
        
        this.vertexPackingList = packing;
        this.vaoBindingFlags = 0;
        
        this.loadingBuffer = uploadBuffer;
        ByteBuffer byteBuffer = uploadBuffer.byteBuffer();
        byteBuffer.position(0);
//            int newMax = Math.max(maxSize, buffer.limit());
//            if(newMax > maxSize)
//            {
//                System.out.println("new max buffer size: " + newMax);
//                maxSize = newMax;
//            }
        OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, this.glBufferId);
        OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STATIC_DRAW);
        OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        APPLEFence.glSetFenceAPPLE(this.glFenceId);
        awaitingFence = true;
    }
    
    /**
     * Renders all uploaded vbos.
     */
    public final void renderChunkTranslucent()
    {
        if(this.isNew)
        {
//            System.out.println("Rendering buffer (translucent) before load");
            return;
        }
        
        final VertexPackingList packing = this.vertexPackingList;
        if(packing.size() == 0) return;
        OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, this.glBufferId);
        reset(false);
        packing.forEach(this);
    }
    
    public final void prepareSolidRender()
    {
        //TODO: remove or make assertion
        if(this.isNew)
        {
//            System.out.println("Rendering buffer (solid) before load");
            return;
        }
        
        reset(true);
        vertexPackingList.reset();
    }
    
    void reset(boolean isSolidLayer)
    {
        bufferOffset = 0;
        vertexOffset = 0;
        lastFormat = null;
        this.isSolidLayer = isSolidLayer;
    }
    
    public final void renderSolidNext()
    {
        if(this.isNew)
            return;
        
        OpenGlHelperExt.glBindBufferFast(OpenGlHelper.GL_ARRAY_BUFFER, this.glBufferId);
        vertexPackingList.renderNext(this);
    }
    
    @SuppressWarnings("null")
    @Override
    public final void accept(RenderPipeline pipeline, int vertexCount)
    {
        pipeline.activate(isSolidLayer);
        if(pipeline.piplineVertexFormat() != lastFormat)
        {
            vertexOffset = 0;
            lastFormat = pipeline.piplineVertexFormat();
            setupAttributes(lastFormat, bufferOffset);
        }
        // Always necessary to rebind attributes in solid because calls to accept are interleaved with calls to other buffers
        // Not needed in translucent because all pipelines in buffer are rendered before moving to next buffer.
        else if(isSolidLayer)
            setupAttributes(lastFormat, bufferOffset);
        
        
        OpenGlHelperExt.glDrawArraysFast(GL11.GL_QUADS, vertexOffset, vertexCount);
        
        vertexOffset += vertexCount;
        bufferOffset += vertexCount * lastFormat.stride;
    }
    
    private void setupAttributes(PipelineVertexFormat format, int bufferOffset)
    {
        int[] vao = this.vaoBufferId;
        if(vao == null)
            setupAttributesInner(format, bufferOffset);
        else
        {
            final int ordinal = format.layerIndex;
            int vaoName = vao[ordinal];
            OpenGlHelperExt.glBindVertexArray(vaoName);
            // single layer format never requires rebinding b/c always starts at 0
            if(ordinal > 0 && (this.vaoBindingFlags & (1 << ordinal)) == 0 )
            {
                final int stride = format.stride;
                OpenGlHelperExt.glVertexPointerFast(3, VertexFormatElement.EnumType.FLOAT.getGlConstant(), stride, bufferOffset);
                format.bindAttributeLocations(bufferOffset);
                this.vaoBindingFlags |= (1 << ordinal);
            }
        }
    }
    
    private void setupAttributesInner(PipelineVertexFormat format, int bufferOffset)
    {
        final int stride = format.stride;
        OpenGlHelperExt.glVertexPointerFast(3, VertexFormatElement.EnumType.FLOAT.getGlConstant(), stride, bufferOffset);
        format.enableAndBindAttributes(bufferOffset);
    }

    public final void deleteGlBuffers()
    {
        IntBuffer vao = this.vaoNames;
        if(vao != null) try
        {
            vao.position(0);
            OpenGlHelperExt.glDeleteVertexArrays(vao);
        }
        catch(Exception e)
        {
            // noop
        }
        
        if (this.glBufferId >= 0)
        {
            OpenGlHelper.glDeleteBuffers(this.glBufferId);
            this.glBufferId = -1;
        }
        if(this.glFenceId >= 0)
        {
            APPLEFence.glDeleteFencesAPPLE(glFenceId);
            this.glFenceId = -1;
        }
    }
}