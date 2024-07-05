package com.emojibot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class BotProperties {
    private static final Properties properties = new Properties();

    // Static execution block
    static {
        Logger logger = LoggerFactory.getLogger("BotProperties");
        try (InputStream input = BotProperties.class.getClassLoader().getResourceAsStream("config.properties")) {
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
}
