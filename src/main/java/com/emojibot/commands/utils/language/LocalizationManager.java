package com.emojibot.commands.utils.language;


import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {
    private ResourceBundle messages;

    public LocalizationManager(Locale locale) {
        // Load the resource bundle for the given locale
        this.messages = ResourceBundle.getBundle("messages", locale);
    }

    public String getMessage(String commandName, String messageKey) {
        // Construct the key using command name and message key
        String key = commandName + "_command." + messageKey;
        return messages.getString(key);
    }
}