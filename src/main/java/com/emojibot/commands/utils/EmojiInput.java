package com.emojibot.commands.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiInput {
    private static final Pattern EMOJI_PATTERN = Pattern.compile("<a?:([a-zA-Z0-9_]+):(\\d+)>");

    /**
     * Extracts the emoji name from the given input.
     * If the input is in the format <:emoji_name:emoji_id>, it extracts the emoji_name.
     * If the input is just the emoji name, it returns the input as is.
     *
     * @param emojiInput The input string containing the emoji in full format or just the emoji name.
     * @return The extracted emoji name.
     */
    public static String extractEmojiName(String emojiInput) {
        Matcher matcher = EMOJI_PATTERN.matcher(emojiInput);

        if (matcher.matches()) {
            // Full emoji format provided: <name : ID>, return the name part 
            return matcher.group(1);
        } else {
            // Only emoji name provided
            return emojiInput;
        }
    }
}