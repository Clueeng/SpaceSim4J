package net.flaily.util;

import com.google.gson.Gson;
import net.flaily.SpaceApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ConfigUtil {

    public static Path getFlailyDevConfigFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            // Windows: C:\Users\flaily\AppData\Roaming\FlailyDev
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = userHome + "\\AppData\\Roaming";
            }
            return Paths.get(appData, "FlailyDev");
        } else {
            // Linux/macOS: ~/.config/flailydev/
            return Paths.get(userHome, ".config", "flailydev");
        }
    }

    public void loadConfig(SpaceApp app, String configName) {
        // clear current planets
        app.reset();
        app.selectedPlanet = null;

        // Load from file
        File jsonFile = new File(String.valueOf(getFlailyDevConfigFolder().toFile().getAbsolutePath() + File.separator + configName + ".json"));
        if(!jsonFile.exists()){
            System.out.println("Config does not exist!");
            System.out.println(jsonFile.getAbsolutePath());
            return;
        }

        Gson config = new Gson();
    }

    private String getTextInFile(File f){
        try {
            Scanner scanner = new Scanner(f);
            StringBuilder textContent = new StringBuilder();
            while(scanner.hasNext()){
                String data = scanner.nextLine();
                textContent.append(data);
            }
            return textContent.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
