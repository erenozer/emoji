package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmojifyCommand extends EmojiCommand {
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Map<String, String> turkishToEnglish = new HashMap<>();

    public EmojifyCommand(Bot bot) {
        super(bot);
        this.name = "emojify";
        this.description = "Emojify your message";
        this.cooldownDuration = 8;

        this.localizedNames.put(DiscordLocale.TURKISH, "yaz");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Mesajınızı emojiler ile yazar");

        OptionData option = new OptionData(OptionType.STRING, "text", "Text to be emojified", true);
        option.setNameLocalization(DiscordLocale.TURKISH, "yazı");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Emojiler ile yazılacak metin");

        this.args.add(option);

        // Initialize the mapping for Letter -> Emoji
        initializeMapping();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String text = Objects.requireNonNull(event.getOption("text")).getAsString();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        if (text.length() > 40) {
            event.reply(String.format(localization.getMsg("emojify_command", "too_long_message"), BotConfig.noEmoji())).setEphemeral(true).queue();
            return;
        }

        text = turToEng(text); // Convert Turkish characters to English equivalents

        StringBuilder emojifiedText = new StringBuilder();

        for (char ch : text.toCharArray()) {
            String mapped = mapping.get(String.valueOf(ch));
            if (mapped != null) {
                emojifiedText.append(mapped);
            } else {
                // Handle unmapped characters here, could add a space or skip them
                emojifiedText.append(" "); // For example, add a space for unmapped characters
            }
        }

        event.reply(emojifiedText.toString()).queue();
    }

    private static void initializeMapping() {
        mapping.put(" ", "   ");
        mapping.put("0", ":zero:");
        mapping.put("1", ":one:");
        mapping.put("2", ":two:");
        mapping.put("3", ":three:");
        mapping.put("4", ":four:");
        mapping.put("5", ":five:");
        mapping.put("6", ":six:");
        mapping.put("7", ":seven:");
        mapping.put("8", ":eight:");
        mapping.put("9", ":nine:");
        mapping.put("!", ":grey_exclamation:");
        mapping.put("?", ":grey_question:");
        mapping.put("#", ":hash:");
        mapping.put("*", ":asterisk:");

        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        for (char c : alphabet.toCharArray()) {
            mapping.put(String.valueOf(c), ":regional_indicator_" + c + ":");
            mapping.put(String.valueOf(Character.toUpperCase(c)), ":regional_indicator_" + c + ":");
        }

        turkishToEnglish.put("ı", "i");
        turkishToEnglish.put("ğ", "g");
        turkishToEnglish.put("ü", "u");
        turkishToEnglish.put("ş", "s");
        turkishToEnglish.put("ö", "o");
        turkishToEnglish.put("ç", "c");
        turkishToEnglish.put("İ", "I");
        turkishToEnglish.put("Ğ", "G");
        turkishToEnglish.put("Ü", "U");
        turkishToEnglish.put("Ş", "S");
        turkishToEnglish.put("Ö", "O");
        turkishToEnglish.put("Ç", "C");
    }

    public static String turToEng(String string) {
        StringBuilder sb = new StringBuilder();
        for (char c : string.toCharArray()) {
            String translatedChar = turkishToEnglish.get(String.valueOf(c));
            sb.append(translatedChar != null ? translatedChar : c);
        }
        return sb.toString();
    }
}