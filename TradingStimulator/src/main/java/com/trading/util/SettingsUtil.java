package com.trading.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class SettingsUtil {
    private static final Logger logger = LogManager.getLogger(SettingsUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String SETTINGS_FILE = "settings.json";

    public static class AppSettings {
        public boolean darkMode = true;
        public String lastUsername = "";
        public int refreshRateMs = 1000;
    }

    public static void saveSettings(AppSettings settings) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SETTINGS_FILE), settings);
            logger.info("Settings saved successfully.");
        } catch (IOException e) {
            logger.error("Failed to save settings.", e);
        }
    }

    public static AppSettings loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                return mapper.readValue(file, AppSettings.class);
            } catch (IOException e) {
                logger.error("Failed to load settings from file.", e);
            }
        }
        return new AppSettings(); // Return defaults
    }
}
