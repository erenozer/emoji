package com.emojibot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class BotProperties {
    private static final Properties properties = new Properties();

    // Static execution block
    static {
        try (InputStream input = BotProperties.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Error! Unable to find config.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    public static boolean getDevMode() {
        return Boolean.parseBoolean(properties.getProperty("DEV_MODE"));
    }
}
