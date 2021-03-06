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

package grondag.acuity.api.pipeline;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Governs how often pipeline shader uniform initializers are called.<p>
 * 
 * In all cases, initializers will only be called if the pipeline is activated
 * and the values are only uploaded if they have changed.
 */
@Environment(EnvType.CLIENT)
public enum UniformUpdateFrequency
{
    /**
     * Uniform initializer only called 1X after load or reload.
     */
    ON_LOAD,

    /**
     * Uniform initializer called 1X per game tick. (20X per second)
     */
    PER_TICK,
    
    /**
     * Uniform initializer called 1X per render frame. (Variable frequency.)
     */
    PER_FRAME
}
