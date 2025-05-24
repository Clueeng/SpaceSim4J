package net.flaily.objects;

import net.flaily.SpaceApp;
import net.flaily.util.Text;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Planet {
    public String name;
    public double mass, radius;
    public float[] position = {0, 10, 0};
    public float[] color = {1.0f, 1.0f, 1.0f};
    public SpaceApp spaceApp;

    public float[] velocity = {0.0f, 0.0f}, acceleration = {0.0f, 0.0f};

    ArrayList<Planet> planetSystem;

    public Planet(String name, double mass, float radius, ArrayList<Planet> planetSystem, SpaceApp spaceApp){
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        // used to move shit around lol
        this.planetSystem = planetSystem;
        this.spaceApp = spaceApp;
    }

    public Planet(String name, double mass, float radius, SpaceApp spaceApp){
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        this.spaceApp = spaceApp;
    }

    public void setSystem(ArrayList<Planet> planetSystem){
        this.planetSystem = planetSystem;
    }
    public void changeColor(float r,float g,float b){
        this.color[0] = r;
        this.color[1] = g;
        this.color[2] = b;
    }

    private void renderPlanet(){
        int segments = 64;
        double theta = (Math.PI * 2 / segments);

        double tangentialFactor = Math.tan(theta);
        double radialFactor = Math.cos(theta);

        float arbitraryNumber = 1000f;
        float screenScale = 1000.0f / 100.0f / arbitraryNumber; // km -> m -> cm (px?)

        double xVert = this.radius * screenScale;
        double yVert = 0;

        glPushMatrix();
        glLineWidth(2f);
        glColor3f(color[0], color[1], color[2]);
        glBegin(GL_LINE_LOOP);
        for(int i = 0; i < segments; i++) {
            glVertex2d(xVert + getX(), yVert + getY());

            double tx = -yVert;
            double ty = xVert;

            xVert += tx * tangentialFactor;
            yVert += ty * tangentialFactor;

            xVert *= radialFactor;
            yVert *= radialFactor;
        }
        glEnd();
        glPopMatrix();
    }
    private void renderName() {
        // Get screen position of the planet's center
        float[] screenPos = spaceApp.worldToScreen(getX(), getY());

        // Set up orthogonal projection for text
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, spaceApp.windowWidth, spaceApp.windowHeight, 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Position text above the circle
        float textScale = 1.0f;
        float textYOffset = -20; // Pixels above the circle

        glTranslatef(screenPos[0] - spaceApp.cameraX, spaceApp.windowHeight - screenPos[1] + spaceApp.cameraY, 0); // Mirror Y position
        glScalef(1, 1, 1); // Flip text vertically

        //Text.drawText(screenPos[0], screenPos[1] + textYOffset, name, textScale);

        Text.drawText(-Text.getTextWidth(name, textScale) / 2f, -textYOffset, name, textScale);
        String debug = "(" + position[0] + ", " + position[1] + ")";
        Text.drawText(-Text.getTextWidth(debug, textScale) / 2f, -textYOffset + 24, debug, textScale);

        // Restore matrices
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    public void render(){
        renderPlanet();
        renderName();
    }

    public void update(float ts) {
        // old dt, it worked but i couldnt change the time
        float dt = (1f / Physics.FRAMES_PER_SECOND) * ts;

        acceleration[0] = 0f;
        acceleration[1] = 0f;

        for(Planet p : this.planetSystem) {
            if(p == this) continue;

            float deltaX = p.getX() - this.getX();
            float deltaY = p.getY() - this.getY();

            float distanceSq = (deltaX * deltaX) + (deltaY * deltaY);
            float dist = (float) Math.sqrt(distanceSq);

            // Subject to change (we dont want to divide by 0 or go crazy)
            if(dist < 1.005f) continue;

            double force = (Physics.GRAVITY_CONST * this.mass * p.mass) / distanceSq;

            float fx = (float)(force * deltaX / dist);
            float fy = (float)(force * deltaY / dist);

            // My boy newton
            // F = m * a → a = F / m
            acceleration[0] += (float) (fx / this.mass);
            acceleration[1] += (float) (fy / this.mass);
        }
        this.velocity[0] += acceleration[0] * dt;
        this.velocity[1] += acceleration[1] * dt;

        this.position[0] += this.velocity[0] * dt;
        this.position[1] += this.velocity[1] * dt;
    }

    public void setX(float x) { position[0] = x; }
    public void setY(float y) { position[1] = y; }
    public void setZ(float z) { position[2] = z; }

    public float getX(){ return position[0]; }
    public float getY(){ return position[1]; }
    public float getZ(){ return position[2]; }

    public static void setOrbitalVelocities(Planet p1, Planet p2) {
        float dx = p2.getX() - p1.getX();
        float dy = p2.getY() - p1.getY();
        float distance = (float) Math.sqrt(dx*dx + dy*dy);
        if (distance == 0) return;  // avoid division by zero
        float v1 = (float) Math.sqrt(Physics.GRAVITY_CONST * p2.mass / distance);
        float v2 = (float) Math.sqrt(Physics.GRAVITY_CONST * p1.mass / distance);
        float ux = dx / distance;
        float uy = dy / distance;
        p1.velocity[0] = -uy * v1;
        p1.velocity[1] = ux * v1;
        p2.velocity[0] = uy * v2;
        p2.velocity[1] = -ux * v2;
    }

    public static void setCircularOrbit(Planet satellite, Planet centralBody) {
        float dx = satellite.getX() - centralBody.getX();
        float dy = satellite.getY() - centralBody.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float speed = (float) Math.sqrt(Physics.GRAVITY_CONST * centralBody.mass / distance);

        float ux = dx / distance;
        float uy = dy / distance;

        satellite.velocity[0] = -uy * speed;
        satellite.velocity[1] = ux * speed;
    }
}
