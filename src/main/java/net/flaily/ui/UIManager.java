package net.flaily.ui;

import java.util.ArrayList;
import java.util.List;

public class UIManager {
    private List<UIElement> elements = new ArrayList<>();

    public void addElement(UIElement element) {
        elements.add(element);
    }

    public void renderAll() {
        for (UIElement e : elements) if (e.visible) e.render();
    }

    public void updateAll() {
        for (UIElement e : elements) if (e.visible) e.update();
    }

    public void handleMouseClick(double x, double y, int button) {
        for (UIElement e : elements) if (e.visible) e.handleMouseClick(x, y, button);
    }

    public void handleKeyPress(int key, int scancode, int action, int mods) {
        for (UIElement e : elements) if (e.visible) e.handleKeyPress(key, scancode, action, mods);
    }

    public void handleMouseMove(double x) {
        for (UIElement e : elements)
            if (e instanceof Slider s) s.handleMouseMove(x);
    }
}
