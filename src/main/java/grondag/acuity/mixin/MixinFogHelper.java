package grondag.acuity.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import grondag.acuity.mixin.extension.FogHelperExt;
import net.minecraft.client.render.FogHelper;

@Mixin(FogHelper.class)
public abstract class MixinFogHelper implements FogHelperExt
{
    @Shadow private float red;
    @Shadow private float green;
    @Shadow private float blue;

    @Override
    public float fogColorRed()
    {
        return red;
    }

    @Override
    public float fogColorGreen()
    {
        return green;
    }

    @Override
    public float fogColorBlue()
    {
        return blue;
    }
}
