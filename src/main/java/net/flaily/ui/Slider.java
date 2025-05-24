package net.flaily.ui;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public class Slider extends UIElement {
    private float min, max, value;
    private boolean dragging = false;

    public Slider(float x, float y, float width, float min, float max, float initial) {
        super(x, y, width, 20);
        this.min = min; this.max = max; this.value = initial;
        this.visible = false;
    }

    @Override
    public void render() {
        System.out.println("its rendering");

        glColor3f(0.4f, 0.4f, 0.4f);
        glBegin(GL_QUADS);
        glVertex2f(x, y + height / 2 - 2);
        glVertex2f(x + width, y + height / 2 - 2);
        glVertex2f(x + width, y + height / 2 + 2);
        glVertex2f(x, y + height / 2 + 2);
        glEnd();

        // curseur
        float pos = x + ((value - min) / (max - min)) * width;
        glColor3f(0.8f, 0.8f, 0.2f);
        glBegin(GL_QUADS);
        glVertex2f(pos - 5, y);
        glVertex2f(pos + 5, y);
        glVertex2f(pos + 5, y + height);
        glVertex2f(pos - 5, y + height);
        glEnd();
    }

    @Override
    public void update() {}

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button) {
        if (contains(mouseX, mouseY)) {
            dragging = (button == GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
    }

    public void handleMouseMove(double mouseX) {
        if (dragging) {
            float relative = (float)((mouseX - x) / width);
            value = Math.max(min, Math.min(max, min + relative * (max - min)));
        }
    }

    @Override
    public void handleKeyPress(int key, int scancode, int action, int mods) {}

    public float getValue() { return value; }
}
