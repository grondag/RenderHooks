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

package grondag.acuity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.platform.GLX;

import grondag.acuity.api.AcuityRuntimeImpl;
import grondag.acuity.api.pipeline.PipelineManagerImpl;
import grondag.acuity.fermion.config.Localization;
import grondag.acuity.opengl.OpenGlHelperExt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;

public class Acuity implements ModInitializer
{
	public static Acuity INSTANCE = new Acuity();
	
	private static boolean glCapabilitiesMet = false;
	private static boolean isEnabled = false;
	
	@Override
    public void onInitialize()
    {
	    AcuityRuntimeImpl.initialize();
	    
        if(!GLX.usePostProcess)
        {
            getLog().warn(Localization.translate("misc.fail_no_shaders"));
            return;
        }
        if(!GLX.isOpenGl21)
        {
            getLog().warn(Localization.translate("misc.fail_opengl_version"));
            return;
        }
        
        OpenGlHelperExt.initialize();
        
        if(!OpenGlHelperExt.areAsynchMappedBuffersSupported())
        {
            getLog().warn(Localization.translate("misc.fail_no_asynch_mapped"));
            return;
        }
        
        getLog().info(Localization.translate("misc.hardware_ok"));
        getLog().info(Localization.translate(OpenGlHelperExt.isVaoEnabled() ? "misc.vao_on" : "misc.vao_off"));
        
        if(!OpenGlHelperExt.isFastNioCopyEnabled())
        {
            getLog().error(Localization.translate("misc.error_no_fast_nio_copy"));
        }
        
        glCapabilitiesMet = true;
        recomputeEnabledStatus();
        
        ResourceManager rm = MinecraftClient.getInstance().getResourceManager();
        if(rm instanceof ReloadableResourceManager)
        {
            ((ReloadableResourceManager)rm).addListener(new ResourceReloadListener() {

                @Override
                public void onResourceReload(ResourceManager resourceManager)
                {
                    AcuityRuntimeImpl.INSTANCE.forceReload();
                }});
        }
        
        ClientTickEvent.CLIENT.register(mc -> 
        {
            PipelineManagerImpl.INSTANCE.onGameTick(mc);
        });
    }
	
    public static final boolean isModEnabled()
    {
        return isEnabled;
    }
	
	public static final void recomputeEnabledStatus()
	{
	    isEnabled = glCapabilitiesMet && Configurator.enabled && OpenGlHelperExt.isFastNioCopyEnabled();
	}
	
    private static Logger log;
    
    public Logger getLog()
    {
        Logger result = log;
        if(result == null)
        {
            result = LogManager.getLogger("Acuity");
            log = result;
        }
        return result;
    }
}
