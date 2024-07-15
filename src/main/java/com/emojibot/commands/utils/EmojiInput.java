package com.emojibot.commands.utils;

import java.text.Normalizer;
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

    public static String extractEmojiId(String emojiInput) {
        Matcher matcher = EMOJI_PATTERN.matcher(emojiInput);

        if (matcher.matches()) {
            // Full emoji format provided: <name : ID>, return the name part 
            return matcher.group(2);
        } else {
            // Only emoji name provided
            return null;
        }
    }

    public static String normalize(String input) {
        return input.trim().replaceAll("\\s+", "");
    }

    /**
     * Removes Turkish characters and other invalid characters from the given string
     * and returns a normalized version that can be used as an emoji name.
     *
     * @param input The input string to normalize.
     * @return The normalized string with invalid characters removed.
     */
    public static String removeInvalidEmojiCharacters(String input) {
        if (input == null) {
            return "";
        }

        // Normalize the string to decompose characters with accents
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Remove accents and diacritical marks
        normalized = normalized.replaceAll("\\p{M}", "");

        // Remove Turkish characters
        normalized = normalized.replace('ç', 'c')
                               .replace('Ç', 'C')
                               .replace('ğ', 'g')
                               .replace('Ğ', 'G')
                               .replace('ı', 'i')
                               .replace('İ', 'I')
                               .replace('ş', 's')
                               .replace('Ş', 'S')
                               .replace('ü', 'u')
                               .replace('Ü', 'U')
                               .replace('ö', 'o')
                               .replace('Ö', 'O');

        // Remove any characters that are not alphanumeric or underscores
        normalized = normalized.replaceAll("[^a-zA-Z0-9_]", "");

        return normalized;
    }

}