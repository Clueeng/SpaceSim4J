package net.flaily.chat;

import net.flaily.SpaceApp;
import net.flaily.objects.MathHelper;
import net.flaily.objects.Planet;
import net.flaily.util.NBTParser;
import net.flaily.util.RenderUtil;
import net.flaily.util.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static net.flaily.util.Text.getTrailingSpacesWidth;
import static org.lwjgl.opengl.GL11.*;

public class ChatBar {

    public int KEY_TOGGLE = GLFW.GLFW_KEY_T;
    public ArrayList<Message> messageList = new ArrayList<>();
    public boolean focused;
    public StringBuilder message = new StringBuilder("");

    public int screenWidth, screenHeight;
    int chatWidth = 480;
    int chatHeight = 64;
    public SpaceApp spaceApp;

    private int cursorPos = 0;
    private long backspacePressedTime = 0;
    private boolean backspaceHeld = false;
    private static final long BACKSPACE_REPEAT_DELAY = 400;
    private static final long BACKSPACE_REPEAT_INTERVAL = 50;
    private long lastBackspaceRepeat = 0;


    public ChatBar(int width, int height, SpaceApp spaceApp) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.spaceApp = spaceApp;
    }
    public void updateDimension(int w, int h){
        this.screenWidth = w;
        this.screenHeight = h;
    }
    public void update() {
        if (backspaceHeld) {
            long now = System.currentTimeMillis();
            if (now - backspacePressedTime > BACKSPACE_REPEAT_DELAY &&
                    now - lastBackspaceRepeat > BACKSPACE_REPEAT_INTERVAL) {
                deleteCharBeforeCursor();
                lastBackspaceRepeat = now;
            }
        }
    }


    public void render(int w, int h){
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, w, h, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        renderChatBox(w, h);

        if(this.focused)
            renderChatField(w, h);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void renderChatBox(int width, int height){

        int space = 24;
        int x = (int) getX();
        int y = (int) getY() - space;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message m = messageList.get(i);
            glColor4f(1f, 1f, 1f, Math.max(0.01f, m.opacity));
            Text.drawText(x + 4, y + 4, m.getMessage());
            y -= space;
            m.updateOpacity();
        }
        glDisable(GL_BLEND);
    }
    private void renderChatField(int width, int height){
        // if(!focused) return;
        glPushMatrix();

        // Just render a box
        glLineWidth(1f);

        int x = (int) getX();
        int y = (int) getY();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0.1f, 0.1f, 0.1f, 0.7f);

        RenderUtil.drawRect(x, y, chatWidth, chatHeight, true);

        glDisable(GL_BLEND);

        if(focused)
            glColor3f(1f, 0f, 0f);
        else
            glColor3f(0f, 1f, 1f);
        RenderUtil.drawRect(x, y, chatWidth, chatHeight, false);

        drawText();
        //Text.drawText(x + 4, y + 4, message.toString() + suffix, 1.75f);

        glPopMatrix();
    }
    private void drawText(){
        int x = (int) getX();
        int y = (int) getY();
        String fullText = message.toString();
        float scale = 1.75f;
        float padding = 4;
        float maxTextWidth = chatWidth - 2 * padding;
        float textWidth = Text.getTextWidth2(fullText, scale) + (Text.getTextWidth2("a", scale) * 1);
        float textX = x + padding;
        if (textWidth > maxTextWidth) {
            textX -= (textWidth - maxTextWidth);
        }

        glEnable(GL_SCISSOR_TEST);

        glScissor(x, 0, chatWidth + 10, screenHeight);

        Text.drawText(textX, y + padding, fullText, scale);


        String beforeCursor = message.substring(0, cursorPos);
        String afterCursor = message.substring(cursorPos);
        float cursorXOffset = Text.getTextWidth2(beforeCursor, scale);
        cursorXOffset += getTrailingSpacesWidth(beforeCursor, scale);

        if (focused) {
            float cursorPosX = textX + cursorXOffset;
            float cursorPosY = y + padding;
            glColor3f(1f, 1f, 1f);
            glLineWidth(2f);
            glBegin(GL_LINES);
            glVertex2f(cursorPosX, cursorPosY);
            glVertex2f(cursorPosX, cursorPosY + 20); // cursor height
            glEnd();
        }

        glDisable(GL_SCISSOR_TEST);
    }

    public double getX() {
        int offset = 14;
        return screenWidth - chatWidth - offset + 4;
    }
    public double getY() {
        int offset = 14;
        return screenHeight - chatHeight - offset;
    }

    int historyIndex;
//    public void handleKeyPress(int key, int scancode, int action, int mods) {
//        if(key == KEY_TOGGLE && action == GLFW.GLFW_PRESS && !focused){
//            focused = true;
//        }
//
//        if (!focused || action != GLFW.GLFW_PRESS) return;
//
//        if(key == GLFW.GLFW_KEY_UP) {
//            historyIndex++;
//            int search = messageList.size() - historyIndex - 1;
//            try{
//                this.message = new StringBuilder(messageList.get(search).text);
//            }catch (IndexOutOfBoundsException e){
//                System.out.println("lol");
//            }
//        }
//
//        if (key == GLFW.GLFW_KEY_BACKSPACE && !message.isEmpty()) {
//            message.deleteCharAt(message.length() - 1);
//        } if(key == GLFW.GLFW_KEY_ENTER && !message.isEmpty()){
//            // send command and add to chat
//            messageList.add(new Message(message.toString()));
//            evaluateCommand(message.toString());
//            message.delete(0, message.length());
//            historyIndex = 0;
//        }
//        else if (key >= 32 && key <= 126) {
//            //message.append((char) key);
//        }
//    }
    public void handleKeyPress(int key, int scancode, int action, int mods) {
        if (key == KEY_TOGGLE && action == GLFW.GLFW_PRESS && !focused) {
            focused = true;
            cursorPos = message.length();
            return;
        }

        if (!focused) return;

        boolean ctrlDown = (mods & GLFW.GLFW_MOD_CONTROL) != 0;

        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            ArrayList<Message> reversed = new ArrayList<>();
            for (int i = messageList.size() - 1; i >= 0; i--) {
                reversed.add(messageList.get(i));
            }

            switch (key) {
                case GLFW.GLFW_KEY_UP -> {
                    historyIndex++;
                    historyIndex = (int)MathHelper.clamp(0, messageList.size() - 1, historyIndex);
                    this.message = new StringBuilder(
                            reversed.get(historyIndex).text
                    );
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    historyIndex--;
                    if(historyIndex == -1){
                        this.message = new StringBuilder();
                        return;
                    }
                    historyIndex = (int)MathHelper.clamp(0, messageList.size() - 1, historyIndex);

                    this.message = new StringBuilder(
                            reversed.get(historyIndex).text
                    );
                }

                case GLFW.GLFW_KEY_LEFT -> {
                    if (ctrlDown) cursorPos = findPrevWord(cursorPos);
                    else if (cursorPos > 0) cursorPos--;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    if (ctrlDown) cursorPos = findNextWord(cursorPos);
                    else if (cursorPos < message.length()) cursorPos++;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cursorPos > 0) {
                        deleteCharBeforeCursor();
                        backspaceHeld = true;
                        backspacePressedTime = System.currentTimeMillis();
                        lastBackspaceRepeat = backspacePressedTime;
                    }
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    if (!message.isEmpty()) {
                        messageList.add(new Message(message.toString()));
                        evaluateCommand(message.toString());
                        message.setLength(0);
                        cursorPos = 0;
                    }
                }
            }
            if(key >= 32 && key <= 126){
                //message.append((char)key);
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                backspaceHeld = false;
            }
        }
    }
    private int findPrevWord(int pos) {
        if (pos == 0) return 0;
        pos--;
        while (pos > 0 && Character.isWhitespace(message.charAt(pos))) pos--;
        while (pos > 0 && !Character.isWhitespace(message.charAt(pos - 1))) pos--;
        return pos;
    }

    private int findNextWord(int pos) {
        int len = message.length();
        if (pos >= len) return len;
        while (pos < len && !Character.isWhitespace(message.charAt(pos))) pos++;
        while (pos < len && Character.isWhitespace(message.charAt(pos))) pos++;
        return pos;
    }

    public void addToChat(Message m){
        messageList.add(m);
    }

    public void evaluateCommand(String s){
        if(!s.startsWith("/")) return;
        String[] splitCommand = s.split(" ");

        if(splitCommand.length < 1) return;
        String base = splitCommand[0].replace("/", "");
        System.out.println(s);
        switch (base){
            case "modify" -> {
                if(spaceApp.selectedPlanet == null){
                    addToChat(new Message("Please select a planet by clicking on one"));
                    return;
                }
                if(splitCommand.length < 3) return;
                // modify <name> <value>
                String property = splitCommand[1].toLowerCase();
                switch (property){
                    case "name" -> {
                        spaceApp.selectedPlanet.name = splitCommand[2];
                    }
                    case "mass" -> {
                        try{
                            spaceApp.selectedPlanet.mass = Float.parseFloat(splitCommand[2]);
                        }catch (NumberFormatException e){
                            addToChat(new Message(String.format("%s is not a valid number", splitCommand[2])));
                        }
                    }
                    case "radius" -> {
                        try{
                            spaceApp.selectedPlanet.radius = Float.parseFloat(splitCommand[2]);
                        }catch (NumberFormatException e){
                            addToChat(new Message(String.format("%s is not a valid number", splitCommand[2])));
                        }
                    }
                    case "position" -> {
                        try{
                            if(splitCommand.length < 4) return;
                            int x = Integer.parseInt(splitCommand[2]);
                            int y = Integer.parseInt(splitCommand[3]);
                            spaceApp.selectedPlanet.position[0] = x;
                            spaceApp.selectedPlanet.position[1] = y;
                        }catch (NumberFormatException e){
                            addToChat(new Message("One value is wrong, " + Arrays.toString(splitCommand)));
                        }
                    }
                    case "velocity" -> {
                        try{
                            if(splitCommand.length < 4) return;
                            int x = Integer.parseInt(splitCommand[2]);
                            int y = Integer.parseInt(splitCommand[3]);
                            spaceApp.selectedPlanet.velocity[0] = x;
                            spaceApp.selectedPlanet.velocity[1] = y;
                        }catch (NumberFormatException e){
                            addToChat(new Message("One value is wrong, " + Arrays.toString(splitCommand)));
                        }
                    }
                    case "color" -> {
                        try{
                            if(splitCommand.length < 5) return;
                            float r = Float.parseFloat(splitCommand[2]);
                            float g = Float.parseFloat(splitCommand[3]);
                            float b = Float.parseFloat(splitCommand[4]);
                            spaceApp.selectedPlanet.color[0] = r / 255f;
                            spaceApp.selectedPlanet.color[1] = g / 255f;
                            spaceApp.selectedPlanet.color[2] = b / 255f;
                        }catch (NumberFormatException e){
                            addToChat(new Message("One value is wrong, " + Arrays.toString(splitCommand)));
                        }
                    }
                }
            }
            case "time" -> {
                if (splitCommand.length < 2) return;
                try{
                    spaceApp.timeScale = Float.parseFloat(splitCommand[1]);
                }catch (NumberFormatException e){
                    addToChat(new Message("Wrong number format"));
                }
            }

            case "copy" -> {
                if(spaceApp.selectedPlanet == null){
                    addToChat(new Message("Select a planet"));
                    return;
                }
                Planet cy = spaceApp.selectedPlanet;
                Planet copied = new Planet(cy.name + " - Copy", cy.mass, cy.radius, spaceApp);
                spaceApp.spawnPlanet(
                        copied
                );
            }

            case "spawn" -> {
                if (splitCommand.length < 2) return;
                String nbtData = s.substring(s.indexOf('{'));
                HashMap<String, String> data = NBTParser.parse(nbtData);

                String name = data.getOrDefault("Name", "Unnamed");
                int x = Integer.parseInt(data.getOrDefault("X", "0"));
                int y = Integer.parseInt(data.getOrDefault("Y", "0"));
                float radius = Float.parseFloat(data.getOrDefault("Radius", "50.0"));
                double mass = Double.parseDouble(data.getOrDefault("Mass", "100.0"));
                float velX = Float.parseFloat(data.getOrDefault("VelX", "0.0"));
                float velY = Float.parseFloat(data.getOrDefault("VelY", "0.0"));

                Planet toSpawn = new Planet(name, mass, radius, spaceApp);
                toSpawn.setX(x);
                toSpawn.setY(y);
                toSpawn.velocity[0] = velX;
                toSpawn.velocity[1] = velY;

                spaceApp.spawnPlanet(toSpawn);
                break;
            }
        }
    }

    public void handleCharInput(int codepoint) {
        if (!focused) return;
        message.insert(cursorPos, Character.toChars(codepoint));
        cursorPos += Character.charCount(codepoint);
    }
    private void deleteCharBeforeCursor() {
        if (cursorPos == 0) return;
        message.deleteCharAt(cursorPos - 1);
        cursorPos--;
    }


    public void handleMouseClick(double x, double y, int mouseButton){
        System.out.println(x + ", " + y + ", " + mouseButton);

        if(mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            focused = x >= getX() && x <= getX() + chatWidth
                    && y >= getY() && y <= getY() + chatHeight;
            if(!focused) historyIndex = 0;
        }
    }

    public static class Message {
        public String text;
        public Date date;
        public long added;
        public float opacity;
        public Message(String text) {
            this.text = text;
            this.date = null; // TODO
            this.opacity = 1.0f;
            this.added = System.currentTimeMillis();
        }

        public String formatDate(){
            return "TODO";
        }
        public String getMessage(){
            return "[" + formatDate() + "] " + this.text;
        }

        public void updateOpacity() {
            long elapsed = System.currentTimeMillis() - this.added;
            if(elapsed > 5000)
                this.opacity -= 0.012f;
        }
    }
}
