package net.flaily.util;

import static org.lwjgl.opengl.GL20.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class ShaderUtils {

    public static int loadShader(String vertexPath, String fragmentPath) {
        String vertexSource = readFile(vertexPath);
        String fragmentSource = readFile(fragmentPath);

        int vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to link shader program:\n" + glGetProgramInfoLog(program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }

    private static int compileShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to compile shader:\n" + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    public static String readFile(String path) {
        try (InputStream in = ShaderUtils.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Shader file not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader file: " + path, e);
        }
    }

}