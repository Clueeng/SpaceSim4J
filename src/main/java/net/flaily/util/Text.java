package net.flaily.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class Text {
    public static float getTextWidth(String text, float scale) {
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);

        float maxX = 0;
        charBuffer.rewind();
        for (int i = 0; i < quads * 4; i++) {
            float vx = charBuffer.get();
            charBuffer.get();
            charBuffer.get();
            charBuffer.get();
            maxX = Math.max(maxX, vx);
        }

        return maxX * scale;
    }

    public static void drawText(float x, float y, String text, float scale) {
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, charBuffer);
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScaled(scale, scale, 1);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glDisableClientState(GL_VERTEX_ARRAY);

        glPopMatrix();
    }

    public static void drawText(int x, int y, String text) {
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 270); // 270 is max buffer per STBEasyFont doc
        int quads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

}
