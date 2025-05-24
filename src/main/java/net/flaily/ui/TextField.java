package net.flaily.ui;

import net.flaily.util.Text;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.*;

public class TextField extends UIElement {
    private StringBuilder text = new StringBuilder();
    private boolean focused = false;

    public TextField(float x, float y, float width) {
        super(x, y, width, 30);
    }

    @Override
    public void render() {
        // dessiner le champ avec le texte
        glColor3f(0.2f, 0.2f, 0.2f); // fond
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor3f(1f, 1f, 1f); // texte
        Text.drawText(x + 5, y + height / 4, text.toString(), 1f);
    }

    @Override
    public void update() {}

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button) {
        focused = contains(mouseX, mouseY);
    }

    @Override
    public void handleKeyPress(int key, int scancode, int action, int mods) {
        if (!focused || action != GLFW.GLFW_PRESS) return;

        if (key == GLFW.GLFW_KEY_BACKSPACE && text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
        } else if (key >= 32 && key <= 126) {
            text.append((char) key); // gestion simplifiée du texte
        }
    }

    public String getText() { return text.toString(); }
}
