package grondag.render_hooks.api;

import static grondag.render_hooks.api.PipelineVertextFormatElements.AO_1B;
import static grondag.render_hooks.api.PipelineVertextFormatElements.BASE_RGBA_4UB;
import static grondag.render_hooks.api.PipelineVertextFormatElements.BASE_TEX_2F;
import static grondag.render_hooks.api.PipelineVertextFormatElements.LIGHTMAP_AND_GLOWS_4UB;
import static grondag.render_hooks.api.PipelineVertextFormatElements.NORMAL_3B;
import static grondag.render_hooks.api.PipelineVertextFormatElements.POSITION_3F;
import static grondag.render_hooks.api.PipelineVertextFormatElements.SECONDARY_RGBA_4UB;
import static grondag.render_hooks.api.PipelineVertextFormatElements.SECONDARY_TEX_2F;
import static grondag.render_hooks.api.PipelineVertextFormatElements.TERTIARY_RGBA_4UB;
import static grondag.render_hooks.api.PipelineVertextFormatElements.TERTIARY_TEX_2F;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public enum PipelineVertexFormat
{
    /**
     * Same as Vanilla Minecraft BLOCK format.
     * Has position, color, lightmap and texture atlas uv.
     */
    MINECRAFT(DefaultVertexFormats.BLOCK),
    
    /**
     * Slightly larger than default MC format, but include AO, glow (for three layers), lightmaps and normals.
     * Lightmaps are passed as scalar value vs half-float texture coordinates, so must use a shader.
     */
    SINGLE(new VertexFormat()
            .addElement(POSITION_3F)
            .addElement(BASE_RGBA_4UB)
            .addElement(BASE_TEX_2F)
            .addElement(NORMAL_3B)
            .addElement(AO_1B)
            .addElement(LIGHTMAP_AND_GLOWS_4UB)),
    
    /**
     * Everything in {@link #SINGLE} plus one extra color and texture coordinate.
     * Use for two-layered textures.
     */
    DOUBLE(new VertexFormat()
            .addElement(POSITION_3F)
            .addElement(BASE_RGBA_4UB)
            .addElement(BASE_TEX_2F)
            .addElement(NORMAL_3B)
            .addElement(AO_1B)
            .addElement(LIGHTMAP_AND_GLOWS_4UB)
            .addElement(SECONDARY_RGBA_4UB)
            .addElement(SECONDARY_TEX_2F)),
    
    /**
     * Everything in {@link #SINGLE} plus two extra colors and texture coordinates.
     * Use for three-layered materials.
     */
    TRIPLE(new VertexFormat()
            .addElement(POSITION_3F)
            .addElement(BASE_RGBA_4UB)
            .addElement(BASE_TEX_2F)
            .addElement(NORMAL_3B)
            .addElement(AO_1B)
            .addElement(LIGHTMAP_AND_GLOWS_4UB)
            .addElement(SECONDARY_RGBA_4UB)
            .addElement(SECONDARY_TEX_2F)
            .addElement(TERTIARY_RGBA_4UB)
            .addElement(TERTIARY_TEX_2F));
    
    public final VertexFormat vertexFormat;
    
    private  PipelineVertexFormat(VertexFormat vertexFormat)
    {
        this.vertexFormat = vertexFormat;
    }
}
