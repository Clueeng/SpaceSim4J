package net.flaily.objects;

public class MathHelper {
    public static float lerp(float a, float b, float f)
    {
        return (float) ((a * (1.0 - f)) + (b * f));
    }
}
