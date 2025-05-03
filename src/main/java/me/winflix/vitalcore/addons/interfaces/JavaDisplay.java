package me.winflix.vitalcore.addons.interfaces;

import java.util.Locale;
import java.util.function.Consumer;

import me.winflix.vitalcore.addons.utils.MathUtils;

public enum JavaDisplay
{
    THIRDPERSON_RIGHTHAND, 
    THIRDPERSON_LEFTHAND, 
    FIRSTPERSON_RIGHTHAND, 
    FIRSTPERSON_LEFTHAND, 
    GROUND, 
    GUI, 
    HEAD, 
    FIXED;
    
    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
    
    public enum Transform
    {
        ROTATION(vals -> {
            final int n = 0;
            vals[n] %= 360.0f;
            final int n2 = 1;
            vals[n2] %= 360.0f;
            final int n3 = 2;
            vals[n3] %= 360.0f;
            return;
        }), 
        TRANSLATION(vals -> {
            vals[0] = MathUtils.clamp(vals[0], -80.0f, 80.0f);
            vals[1] = MathUtils.clamp(vals[1], -80.0f, 80.0f);
            vals[2] = MathUtils.clamp(vals[2], -80.0f, 80.0f);
            return;
        }), 
        SCALE(vals -> {
            vals[0] = MathUtils.clamp(vals[0], 0.0f, 4.0f);
            vals[1] = MathUtils.clamp(vals[1], 0.0f, 4.0f);
            vals[2] = MathUtils.clamp(vals[2], 0.0f, 4.0f);
            return;
        });
        
        private final Consumer<float[]> sanitizer;
        
        public void sanitize(final float[] val) {
            this.sanitizer.accept(val);
        }
        
        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
        
        private Transform(final Consumer<float[]> sanitizer) {
            this.sanitizer = sanitizer;
        }
    }
}
