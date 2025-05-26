package net.flaily.util;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil {

    public static void drawRect(int x, int y, int width, int height, boolean filled){
        if(filled){
            drawFilledRect(x, y, width, height);
        }else{
            drawUnfilledRect(x, y, width, height);
        }
    }

    private static void drawFilledRect(int x, int y, int w, int h){
        glBegin(GL_POLYGON);

        glVertex2i(x, y);
        glVertex2i(x + w, y);
        glVertex2i(x + w, y + h);
        glVertex2i(x, y + h);

        glEnd();
    }
    private static void drawUnfilledRect(int x, int y, int w, int h){
        glBegin(GL_LINE_LOOP);

        glVertex2i(x, y);
        glVertex2i(x + w, y);
        glVertex2i(x + w, y + h);
        glVertex2i(x, y + h);

        glEnd();
    }

}
