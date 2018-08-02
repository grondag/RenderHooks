package grondag.acuity.api;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PipelineFragmentShader extends AbstractPipelineShader implements IPipelineFragmentShader
{
    PipelineFragmentShader(String fileName, TextureFormat textureFormat)
    {
        super(fileName, OpenGlHelper.GL_FRAGMENT_SHADER, textureFormat);
    }
    
    @Override
    public String getSource()
    {
        String result = super.getSource();
        result = result.replaceAll("#version\\s+120", "");
        return PipelineShaderManager.INSTANCE.fragmentLibrarySource + result;
    }
}
