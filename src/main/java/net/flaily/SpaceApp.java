package net.flaily;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImFloat;
import net.flaily.chat.ChatBar;
import net.flaily.objects.MathHelper;
import net.flaily.objects.Planet;
import net.flaily.ui.Slider;
import net.flaily.ui.UIManager;
import net.flaily.util.ShaderUtils;
import net.flaily.util.Text;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;


import java.nio.*;
import java.util.ArrayList;
import java.util.Random;

import static net.flaily.util.Text.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SpaceApp {

    public long windowHandle = -1L;


    private UIManager uiManager = new UIManager();
    private final int WINDOW_WIDTH = 640, WINDOW_HEIGHT = 420;

    private float lerpCameraX = 0f, lerpCameraY = 0f;
    public float cameraX = 0f;
    public float cameraY = 0f;
    public float zoom = .005f;
    public int windowWidth = WINDOW_WIDTH;
    public int windowHeight = WINDOW_HEIGHT;
    private ChatBar chatBar = new ChatBar(windowWidth, windowHeight, this);
    private float CAMERA_SPEED = 0.1f;
    private float ZOOM_SPEED = 0.25f;

    ImVec2 currentWindowPos = new ImVec2(0, 0);
    ImVec2 currentWindowSize = new ImVec2(10, 10);

    private boolean grabbingPlanet;

    public boolean following;

    int backgroundShaderID = -1;
    long startTime = System.nanoTime();

    // UI stuff for later
    private boolean menuOpen = false;

    public float timeScale = 1.0f, oldTimeScale = 1.0f;
    public Planet selectedPlanet = null;
    private boolean addingPlanetMode = false;
    private float newPlanetMass = 1e6f;
    private float newPlanetRadius = 100f;
    private boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private double mouseX, mouseY;
    private boolean sliderDragging = false;

    // UI
    Slider timeScaleSlider;

    private final boolean[] keys = new boolean[GLFW_KEY_LAST + 1];


    ArrayList<Planet> planets = new ArrayList<>();

    public SpaceApp() {
        System.out.println("Initializing GLFW " + Version.getVersion());

        /*
         * Initializes the window
         * We will use GLFW
        */
        GLFWErrorCallback.createPrint(System.err).set();

        initialize();

        /*
         * Main Loop
         */
        GL.createCapabilities();
        glLoadIdentity();

        // Setting up ImGUI
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
        ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330 core");

        updateProjection();
        //glOrtho(-WINDOW_WIDTH / 2f, WINDOW_WIDTH / 2f, -WINDOW_HEIGHT / 2f, WINDOW_HEIGHT / 2f, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        glClearColor(.1f, .1f, .1f, 0.0f);

        generatePLanets();

        double lastTime = glfwGetTime();

        backgroundShaderID = ShaderUtils.loadShader("galaxy.vert", "galaxy.frag");

        while(!glfwWindowShouldClose(windowHandle)){
            double currentTime = glfwGetTime();
            float deltaTime = (float) ((currentTime - lastTime));

            // Update elements
            timeScaleSlider.visible = menuOpen;

            uiManager.updateAll();

            if (keys[GLFW_KEY_UP] || keys[GLFW_KEY_W]) {
                lerpCameraY += CAMERA_SPEED * deltaTime;
            }
            if (keys[GLFW_KEY_DOWN] || keys[GLFW_KEY_S]) {
                lerpCameraY -= CAMERA_SPEED * deltaTime;
            }
            if ((keys[GLFW_KEY_LEFT] && !menuOpen) || keys[GLFW_KEY_A]) {
                lerpCameraX -= CAMERA_SPEED * deltaTime;
            }
            if ((keys[GLFW_KEY_RIGHT] && !menuOpen) || keys[GLFW_KEY_D]) {
                lerpCameraX += CAMERA_SPEED * deltaTime;
            }
            if (keys[GLFW_KEY_EQUAL]) {
                zoom *= (float) Math.pow(1.0 + ZOOM_SPEED, deltaTime);
            }
            if (keys[GLFW_KEY_MINUS]) {
                zoom /= (float) Math.pow(1.01, deltaTime);
            }
            if(following){
                if(selectedPlanet != null) {
                    float screenScale = 1.0f / zoom; // km -> m -> cm (px?)
                    lerpCameraX = selectedPlanet.getX() / screenScale;
                    lerpCameraY = selectedPlanet.getY() / screenScale;
                }
            }

            if(grabbingPlanet && selectedPlanet != null) {

            }

            if(addingPlanetMode && mouseButtons[GLFW_MOUSE_BUTTON_LEFT]) {
                float worldX = (float)((mouseX - windowWidth/2f) / zoom + cameraX);
                float worldY = (float)((mouseY - windowHeight/2f) / zoom + cameraY);

                Planet newPlanet = new Planet("New Planet", newPlanetMass, newPlanetRadius, planets, this);
                newPlanet.setX(worldX);
                newPlanet.setY(worldY);
                addPlanet(newPlanet);
                addingPlanetMode = false;
            }


            // ImGui Start Frame
            imGuiGlfw.newFrame();
            ImGui.newFrame();


            updateProjection();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderGrid();
            applyShader();


            for(Planet p : planets){
                p.render();
                // update for time scale later in here
                //p.update(deltaTime * timeScale); // example
                p.update(timeScale);
            }
            chatBar.update();

            // Save current matrices
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, windowWidth, windowHeight, 0, -1, 1); // Screen coordinates

            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            // Enable blending for text
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            // Draw text in screen coordinates (4,4 is top-left corner)
            glColor3f(1f, 1f, 1f);


            if (menuOpen) {
                renderOverlay();
            }
            Text.drawText(20, 20, "Camera: " + cameraX + ", " + cameraY, 1.0f);
            Text.drawText(20, 40, "Mouse: " + mouseX + ", " + mouseY, 1.0f);
            Text.drawText(20, 60, "Mouse World: " + (mouseX + cameraX) + ", " + (mouseY + cameraY), 1.0f);

            float arbitraryNumber = 1000f;
            float screenScale = 10f / arbitraryNumber;

            Text.drawText(20, 80, "Mouse World2: " + ((mouseX + cameraX) * screenScale) + ", " + ((mouseY + cameraY) * screenScale), 1.0f);
            Text.drawText(20, 100, "Zoom: " + zoom, 1.0f);
            float tw = Text.getTextWidth2("Time Scale: " + timeScale, 2f);
            Text.drawText(windowWidth - 10 - tw, 10, "Time Scale: " + timeScale, 2f);


            // Restore state
            glDisable(GL_BLEND);
            glPopMatrix();

            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
            uiManager.renderAll();
            chatBar.render(windowWidth, windowHeight);

            // ImGUI After render
            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }

        /*
         * Terminate the application
         *
         */
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private Planet getPlanet(int i) {
        Random r = new Random();
        int bounds = 20000;
        double maxMass = 1e7;
        double maxRadius = 1000 * 80;

        int xRand = (int) ((r.nextFloat() * bounds) - (bounds / 2f));
        int yRand = (int) ((r.nextFloat() * bounds) - (bounds / 2f));
        double randMass = (r.nextFloat() * maxMass);
        int randRad = (int) (r.nextFloat() * maxRadius);

        int randomR = (int) (r.nextFloat() * 255);
        int randomG = (int) (r.nextFloat() * 255);
        int randomB = (int) (r.nextFloat() * 255);

        Planet randomPlanet = new Planet("Random"+ i, randMass, randRad, planets, this);
        randomPlanet.setX(xRand);
        randomPlanet.setY(yRand);
        randomPlanet.changeColor(randomR, randomG, randomB);
        return randomPlanet;
    }

    private void initialize(){
        if(!glfwInit()) throw new IllegalStateException("Could not initialize GLFW");

        uiManager.addElement(
                timeScaleSlider = new Slider(4, 10, 100, 10, 200, 100)
        );

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        windowHandle = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Window", NULL, NULL);
        if(windowHandle == NULL) throw new RuntimeException("Could not create Window");

        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });
        glfwSetCharCallback(windowHandle, (windowHandle, codepoint) -> {
            chatBar.handleCharInput(codepoint);
        });

        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            System.out.println("clicked");
        });

        // Key handler callback
        glfwSetKeyCallback(windowHandle, (window, key, scanCode, action, mods) -> {
            chatBar.handleKeyPress(key, scanCode, action, mods);
            uiManager.handleKeyPress(key, scanCode, action, mods);

            if (key >= 0 && key <= GLFW_KEY_LAST) {
                if (action == GLFW_PRESS) {
                    if(!chatBar.focused)
                        keys[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_PAGE_UP -> timeScale += 0.2f;
                    case GLFW_KEY_PAGE_DOWN -> timeScale -= 0.2f;

                    case GLFW_KEY_ESCAPE -> {
                        if (chatBar.focused) {
                            chatBar.focused = false;
                        } else {
                            glfwSetWindowShouldClose(window, true);
                        }
                    }
                }
                // timeScale = MathHelper.clamp(0.0f, 10.0f, timeScale);

                int mult = (int) Math.pow(10, 2);

                timeScale = (float) Math.round(timeScale * mult) / mult;
            }
            if (action == GLFW_PRESS || action == GLFW_KEY_DOWN) {

                switch (key) {
                    case GLFW_KEY_F -> {
                        if(chatBar.focused) return;
                        if(selectedPlanet == null){
                            chatBar.addToChat(new ChatBar.Message("Select a planet to follow"));
                            return;
                        }
                        following = !following;
                    }
                    case GLFW_KEY_R -> { // recenter
                        if(chatBar.focused) return;
                        lerpCameraX = 0f;
                        lerpCameraY = 0f;
                        if(selectedPlanet != null) {
                            float arbitraryNumber = 1000f;
                            float screenScale = 1.0f / zoom; // km -> m -> cm (px?)
                            lerpCameraX = selectedPlanet.getX() / screenScale;
                            lerpCameraY = selectedPlanet.getY() / screenScale;
                        }
                    }
                    case GLFW_KEY_TAB -> {
                        menuOpen = !menuOpen;
                    }
                    case GLFW_KEY_SPACE -> {
                        if(chatBar.focused) return;
                        if(timeScale == 0.0f) {
                            timeScale = oldTimeScale;
                        }else{
                            oldTimeScale = timeScale;
                            timeScale = 0.0f;
                        }
                    }
                }
            }
        });
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            mouseButtons[button] = action == GLFW_PRESS;

            if (menuOpen) {
                if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                    // Handle slider drag
                    if(isMouseOver(20, 50, 260, 20)) {
                        sliderDragging = true;
                    }

                    // Handle add planet button
                    if(isMouseOver(20, 450, 100, 30)) {
                        addingPlanetMode = !addingPlanetMode;
                    }
                }
                if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
                    sliderDragging = false;
                }
            }
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            glfwGetCursorPos(window, mouseX, mouseY);

            uiManager.handleMouseClick(mouseX[0], mouseY[0], button);
            chatBar.handleMouseClick(mouseX[0], mouseY[0], button);

            if(action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_LEFT){
                grabbingPlanet = false;
            }

            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if(action == GLFW_RELEASE) return;
                if(mouseX[0] >= chatBar.getX() && mouseY[0] >= chatBar.getY()) return;
                Planet closestPlanet = null;
                float minDistSq = Float.MAX_VALUE;
                for (Planet p : planets) {
                    // System.out.println("------------------------------");
                    // System.out.println(p.name);

                    float[] scr = worldToScreen(p.getX(), p.getY());
                    float scrX = scr[0] - cameraX;
                    float scrY = windowHeight - scr[1] + cameraY;

                    float dx = (float) (scrX - mouseX[0]);
                    float dy = (float) (scrY - mouseY[0]);

                    float distSq = dx * dx + dy * dy;
                    float scaledRadius = (float)(p.radius * zoom);
                    float radiusSq = scaledRadius * scaledRadius;

                    if (distSq < radiusSq && distSq < minDistSq) {
                        closestPlanet = p;
                        minDistSq = distSq;
                    }


                    // System.out.println("------------------------------");
                }
                if (closestPlanet != null) {
                    // check if we're not clicking in the menu
                    boolean horizontal = mouseX[0] >= currentWindowPos.x && mouseX[0] <= currentWindowPos.x + currentWindowSize.x;
                    boolean vertical = mouseY[0] >= currentWindowPos.y && mouseY[0] <= currentWindowPos.y + currentWindowSize.y;
                    if(menuOpen && vertical && horizontal){
                        chatBar.addToChat(new ChatBar.Message("clicking in menu"));
                        return;
                    }

                    selectedPlanet = closestPlanet;
                    grabbingPlanet = true;
                    // System.out.println("Selected closest planet: " + closestPlanet.name + " with distance: " + Math.sqrt(minDistSq));
                }
            }
        });

        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) ->{
            zoom *= (float) Math.pow(1.1, yoffset);
        });

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            glViewport(0, 0, width, height);
            this.windowWidth = width;
            this.windowHeight = height;
            chatBar.updateDimension(width, height);
            updateProjection();
        });

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(windowHandle, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1);
        glfwShowWindow(windowHandle);
    }

    public void addPlanet(Planet planet){
        planet.setSystem(planets);
        this.planets.add(planet);
        System.out.println("Registered " + planet.name);
    }
    public void addPlanets(Planet... planet){
        for(Planet p : planet){
            addPlanet(p);
        }
    }
    public void reset(){
        planets.clear();

    }

    private void updateProjection() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        cameraX = MathHelper.lerp(cameraX, lerpCameraX, 0.1f);
        cameraY = MathHelper.lerp(cameraY, lerpCameraY, 0.1f);
        glOrtho(
                (cameraX - windowWidth / 2f) / zoom,
                (cameraX + windowWidth / 2f) / zoom,
                (cameraY - windowHeight / 2f) / zoom,
                (cameraY + windowHeight / 2f) / zoom,
                -1, 1
        );
        CAMERA_SPEED = .0017f / zoom;
        glMatrixMode(GL_MODELVIEW);
    }

    private void renderGrid() {
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0.75f, 0.75f, 0.75f, 0.05f);

        float gridSpacing = 420f;

        float left = (cameraX - windowWidth / 2f) / zoom;
        float right = (cameraX + windowWidth / 2f) / zoom;
        float bottom = (cameraY - windowHeight / 2f) / zoom;
        float top = (cameraY + windowHeight / 2f) / zoom;

        glBegin(GL_LINES);

        int startX = (int) Math.floor(left / gridSpacing) * (int) gridSpacing;
        int endX = (int) Math.ceil(right / gridSpacing) * (int) gridSpacing;
        for (int x = startX; x <= endX; x += (int) gridSpacing) {
            glVertex2f(x, bottom);
            glVertex2f(x, top);
        }

        int startY = (int) Math.floor(bottom / gridSpacing) * (int) gridSpacing;
        int endY = (int) Math.ceil(top / gridSpacing) * (int) gridSpacing;
        for (int y = startY; y <= endY; y += (int) gridSpacing) {
            glVertex2f(left, y);
            glVertex2f(right, y);
        }
        glDisable(GL_BLEND);
        glEnd();

        glPopMatrix();

        glColor4f(1f, 1f, 1f, 1f);
    }

    private void renderOverlay() {
        /*glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glColor4f(0.4f, 0.4f, 0.4f, 0.4f);
        RenderUtil.drawRect(0, 0, 200, windowHeight, true);

        glColor4f(1f, 1f, 1f, 1f);

        if(selectedPlanet == null){
            Text.drawText(10, 10, "No Planet Selected", 2f);
        }else{
            Text.drawText(10, 10, "Name: " + selectedPlanet.name, 2f);
            Text.drawText(10, 30, "Mass: " + selectedPlanet.mass, 1f);
            Text.drawText(10, 50, "Radius: " + selectedPlanet.radius, 1f);
            Text.drawText(10, 70, "Position: " + "(" + selectedPlanet.position[0] + ", " + selectedPlanet.position[1] + ")", 1f);
            Text.drawText(10, 90, "Velocity: " + "(" + selectedPlanet.velocity[0] + ", " + selectedPlanet.velocity[1] + ")", 1f);
            Text.drawText(10, 110, "Color: " + "(" + selectedPlanet.color[0] + ", " + selectedPlanet.color[1]  + ", " + selectedPlanet.color[2] + ")", 1f);
        }

        glDisable(GL_BLEND);
        glColor3f(1f, 1f, 1f);*/
        if (menuOpen) {
            if(selectedPlanet == null){
                ImGui.begin("Select a planet");
                this.currentWindowPos = ImGui.getWindowPos();
                this.currentWindowSize = ImGui.getWindowSize();
                ImGui.end();
                return;
            }
            ImGui.begin(selectedPlanet.name);
            this.currentWindowPos = ImGui.getWindowPos();
            this.currentWindowSize = ImGui.getWindowSize();

            float[] mass = { (float) selectedPlanet.mass };
            float[] radius = { (float) selectedPlanet.radius };
            ImFloat posX = new ImFloat(selectedPlanet.getX());
            ImFloat posY = new ImFloat(selectedPlanet.getY());

            if (ImGui.sliderFloat("Mass", mass, 1e3f, 1e9f)) {
                selectedPlanet.mass = mass[0];
            }
            if (ImGui.sliderFloat("Radius", radius, 10f, 5000f)) {
                selectedPlanet.radius = radius[0];
            }
            if (ImGui.inputFloat("X Position", posX)) {
                selectedPlanet.setX(posX.get());
            }
            if (ImGui.inputFloat("Y Position", posY)) {
                selectedPlanet.setY(posY.get());
            }

            // Optional: display info
            ImGui.text("Velocity: " + selectedPlanet.velocity[0] + ", " + selectedPlanet.velocity[1]);

            ImGui.end();
        }
    }

    private boolean isMouseOver(int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public float[] worldToScreen(float worldX, float worldY) {
        float screenX = (worldX - cameraX) * zoom + windowWidth / 2f;
        float screenY = (worldY - cameraY) * zoom + windowHeight / 2f;
        return new float[] {screenX, screenY};
    }

    private void generatePLanets(){
        Planet planet1 = new Planet("Candidate1", 10 * Math.pow(10, 6), 200 * 300, planets, this);
        planet1.changeColor(1f, 1f, 1f);
        planet1.setX(8000);
        planet1.setY(1000);

        Planet planet2 = new Planet("Candidate2", 3 * Math.pow(10, 4), 100 * 300, planets, this);
        planet2.changeColor(1f, 0f, 1f);
        planet2.setX(-9000);
        planet2.setY(900);

        Planet SUN = new Planet("Sun", 10 * Math.pow(10, 11), 1000 * 400, planets, this);
        SUN.changeColor(0.4f, 0.05f, 0.9f);
        SUN.setX(23000);
        SUN.setY(2600);

        Planet.setCircularOrbit(planet1, SUN);
        Planet.setCircularOrbit(planet2, SUN);
        addPlanets(planet1, planet2, SUN);
    }

    public void spawnPlanet(Planet p){
        p.setSystem(planets);
        addPlanet(p);
    }

    public void applyShader() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // glDisable(GL_DEPTH_TEST); // ensure it renders over everything

        glUseProgram(backgroundShaderID);

        float currentTime = (System.nanoTime() - startTime) / 1_000_000_000.0f;

        // Pass uniform values to the shader
        glUniform1f(glGetUniformLocation(backgroundShaderID, "u_time"), currentTime);
        glUniform2f(glGetUniformLocation(backgroundShaderID, "u_resolution"), windowWidth, windowHeight);
        glUniform2f(glGetUniformLocation(backgroundShaderID, "u_camera"), cameraX, cameraY);

        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(windowWidth, 0);
        glVertex2f(windowWidth, windowHeight);
        glVertex2f(0, windowHeight);
        glEnd();

        glUseProgram(0);

        // glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }
}
