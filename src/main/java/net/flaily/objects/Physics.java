package net.flaily.objects;

public class Physics {
    public static int FRAMES_PER_SECOND = 24;
    public static double GRAVITY_SCALE = 5 * Math.pow(10, -5);

    public static double EARTH_MASS = (5.972 * Math.pow(10, 24)) / 10000;
    public static double MOON_MASS = (7.34767309 * Math.pow(10, 22)) / 10000;

    public static double GRAVITY_CONST = 9.81F * GRAVITY_SCALE;

    public static double calculateGravity(float mass, float radius, float distance){
        return mass * distance * Math.pow(radius, 2);
    }

}
