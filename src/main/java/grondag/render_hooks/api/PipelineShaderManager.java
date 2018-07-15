package grondag.render_hooks.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

final class PipelineShaderManager implements IPipelineShaderManager
{
    final static PipelineShaderManager INSTANCE = new PipelineShaderManager();
    private Object2ObjectOpenHashMap<String, PipelineVertexShader> vertexShaders = new Object2ObjectOpenHashMap<>();
    private Object2ObjectOpenHashMap<String, PipelineFragmentShader> fragmentShaders = new Object2ObjectOpenHashMap<>();

    final IPipelineVertexShader[] defaultVertex = new IPipelineVertexShader[PipelineVertexFormat.values().length];
    final IPipelineFragmentShader[] defaultFragment = new IPipelineFragmentShader[PipelineVertexFormat.values().length];;
    
    PipelineShaderManager()
    {
        //FIXME: put in real names for double & triple
        this.defaultVertex[PipelineVertexFormat.COMPATIBLE.ordinal()] = this.getOrCreateVertexShader("/assets/render_hooks/shader/passthru.vert");
        this.defaultVertex[PipelineVertexFormat.SINGLE.ordinal()] = this.getOrCreateVertexShader("/assets/render_hooks/shader/default_single.vert");
        this.defaultVertex[PipelineVertexFormat.DOUBLE.ordinal()] = this.getOrCreateVertexShader("/assets/render_hooks/shader/default_single.vert");
        this.defaultVertex[PipelineVertexFormat.TRIPLE.ordinal()] = this.getOrCreateVertexShader("/assets/render_hooks/shader/default_single.vert");
        this.defaultFragment[PipelineVertexFormat.COMPATIBLE.ordinal()] = this.getOrCreateFragmentShader("/assets/render_hooks/shader/passthru.frag");
        this.defaultFragment[PipelineVertexFormat.SINGLE.ordinal()] = this.getOrCreateFragmentShader("/assets/render_hooks/shader/default_single.frag");
        this.defaultFragment[PipelineVertexFormat.DOUBLE.ordinal()] = this.getOrCreateFragmentShader("/assets/render_hooks/shader/default_single.frag");
        this.defaultFragment[PipelineVertexFormat.TRIPLE.ordinal()] = this.getOrCreateFragmentShader("/assets/render_hooks/shader/default_single.frag");
    }
    
    @SuppressWarnings("null")
    @Override
    public @Nullable IPipelineVertexShader getOrCreateVertexShader(@Nonnull String shaderFileName)
    {
        if(shaderFileName == null || shaderFileName.isEmpty()) 
            return null;
        
        synchronized(vertexShaders)
        {
            PipelineVertexShader result = vertexShaders.get(shaderFileName);
            if(result == null)
            {
                result = new PipelineVertexShader(shaderFileName);
                vertexShaders.put(shaderFileName, result);
            }
            return result;
        }
    }

    @SuppressWarnings("null")
    @Override
    public @Nullable IPipelineFragmentShader getOrCreateFragmentShader(@Nonnull String shaderFileName)
    {
        if(shaderFileName == null || shaderFileName.isEmpty()) 
            return null;
        
        synchronized(fragmentShaders)
        {
            PipelineFragmentShader result = fragmentShaders.get(shaderFileName);
            if(result == null)
            {
                result = new PipelineFragmentShader(shaderFileName);
                fragmentShaders.put(shaderFileName, result);
            }
            return result;
        }
    }

    @Override
    public IPipelineVertexShader getDefaultVertexShader(PipelineVertexFormat format)
    {
        return this.defaultVertex[format.ordinal()];
    }

    @Override
    public IPipelineFragmentShader getDefaultFragmentShader(PipelineVertexFormat format)
    {
        return this.defaultFragment[format.ordinal()];
    }
    
    public void forceReload()
    {
        this.fragmentShaders.values().forEach(s -> s.forceReload());
        this.vertexShaders.values().forEach(s -> s.forceReload());
    }
}