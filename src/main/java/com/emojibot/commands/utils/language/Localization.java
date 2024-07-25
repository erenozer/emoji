package com.emojibot.commands.utils.language;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {
    private ResourceBundle messages;

    public Localization(Locale locale) {
        // Load the resource bundle for the given locale
        this.messages = ResourceBundle.getBundle("messages", locale);
    }

    public String getMsg(String name, String messageKey) {
        // Construct the key using command name and message key
        String key = String.format("%s.%s", name, messageKey);
        return messages.getString(key);
    }

    public static Localization getLocalization(String userId) {
        Locale locale = LanguageManager.getUserLanguageLocal(userId);
        return new Localization(locale);
    }

}