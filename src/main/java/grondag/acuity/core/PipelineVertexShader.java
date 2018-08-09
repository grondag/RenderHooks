package grondag.acuity.core;

import grondag.acuity.api.TextureFormat;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PipelineVertexShader  extends AbstractPipelineShader
{
    PipelineVertexShader(String fileName, TextureFormat textureFormat, boolean isSolidLayer)
    {
        super(fileName, OpenGlHelper.GL_VERTEX_SHADER, textureFormat, isSolidLayer);
    }
    
    @Override
    public String getSource()
    {
        return buildSource(PipelineShaderManager.INSTANCE.vertexLibrarySource);
    }
}
