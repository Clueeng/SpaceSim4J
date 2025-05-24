package net.flaily.ui;

public abstract class UIElement {
    protected float x, y, width, height;
    public boolean visible = true;

    public UIElement(float x, float y, float width, float height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public abstract void render();
    public abstract void update();
    public abstract void handleMouseClick(double mouseX, double mouseY, int button);
    public abstract void handleKeyPress(int key, int scancode, int action, int mods);
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}