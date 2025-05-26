package net.flaily.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class Text {
    public static float getTextWidth(String text, float scale) {
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 460);
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
    public static float getTextWidth2(String text, float scale) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(text.length() * 460);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);
        buffer.rewind();
        FloatBuffer floatBuffer = buffer.asFloatBuffer();

        float maxX = 0;
        for (int i = 0; i < quads * 4; i++) {
            float x = floatBuffer.get();  // x
            float y = floatBuffer.get();  // y
            float z = floatBuffer.get();  // ignored
            float w = floatBuffer.get();  // ignored
            maxX = Math.max(maxX, x);
        }

        return maxX * scale;
    }

    public static float getTrailingSpacesWidth(String str, float scale) {
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) == ' ') count++;
            else break;
        }
        return count * Text.getTextWidth2(" ", scale);
    }
    public static void drawText(float x, float y, String text, float scale) {
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 460);
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
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * 460); // 270 is max buffer per STBEasyFont doc
        int quads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, 16, charBuffer);
        glDrawArrays(GL_QUADS, 0, quads * 4);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

}
