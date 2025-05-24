package net.flaily.ui;

import net.flaily.util.Text;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public class Button extends UIElement {
    private String label;
    private Runnable onClick;

    public Button(float x, float y, float width, float height, String label, Runnable onClick) {
        super(x, y, width, height);
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render() {
        glColor3f(0.3f, 0.3f, 0.3f); // fond
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor3f(1f, 1f, 1f); // texte
        Text.drawText(x + 5, y + height / 4, label, 1f);
    }

    @Override
    public void update() {}

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && contains(mouseX, mouseY)) {
            onClick.run();
        }
    }

    @Override
    public void handleKeyPress(int key, int scancode, int action, int mods) {}
}