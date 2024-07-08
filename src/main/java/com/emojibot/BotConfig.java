package com.emojibot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;
import java.util.Properties;


public class BotConfig {
    private static final Properties properties = new Properties();

    // Static execution block
    static {
        Logger logger = LoggerFactory.getLogger("BotProperties");
        try (InputStream input = BotConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Unable to find config.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            logger.error(ex.toString());
        }
    }

    public static boolean getDevMode() {
        return Boolean.parseBoolean(properties.getProperty("DEV_MODE"));
    }

    public static Color getGeneralEmbedColor() {
        try {
            return Color.decode(properties.getProperty("GENERAL_EMBED_COLOR"));
        } catch (NumberFormatException e) {
            // Handle the case where the string is not a valid hex color
            // This could log an error or return a default color
            return Color.CYAN; 
        }
    }
   
    public static String yesEmoji() {
        return properties.getProperty("YES_EMOJI");
    }

    public static String noEmoji() {
        return properties.getProperty("NO_EMOJI");
    }

    public static String infoEmoji() {
        return properties.getProperty("INFO_EMOJI");
    }
    
}
