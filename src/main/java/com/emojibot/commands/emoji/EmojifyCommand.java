package com.emojibot.commands.emoji;

import com.emojibot.EmojiCache;
import com.emojibot.commands.Command;
import com.emojibot.Bot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmojifyCommand extends Command {
    private static final HashMap<String, String> mapping = new HashMap<>();

    public EmojifyCommand(Bot bot) {
        super(bot);
        this.name = "emojify";
        this.description = "Emojifys the message you typed. Like: \uD83C\uDDED\uD83C\uDDEA\uD83C\uDDF1\uD83C\uDDF1\uD83C\uDDF4";

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "text", "Text to be emojified", true);
        this.args.add(emojiNameArgument);
        initializeMapping();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String text = Objects.requireNonNull(event.getOption("text")).getAsString();
        final String[] text2 = {""};
        Arrays.stream(text.split("")).forEach(a -> {
            String mapped = mapping.get(a);
            if (mapped == null) text2[0] = text2[0] + a;
            else text2[0] += mapped;
        });

        event.reply(text2[0]).queue();

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
    }

    public static String turToEng(String string) {
        HashMap<String, String> toTranslate = new HashMap<>();
        toTranslate.put("ı", "i");
        toTranslate.put("ğ", "g");
        toTranslate.put("ü", "u");
        toTranslate.put("ş", "s");
        toTranslate.put("ö", "o");
        toTranslate.put("ç", "c");
        toTranslate.put("İ", "I");
        toTranslate.put("Ğ", "G");
        toTranslate.put("Ü", "U");
        toTranslate.put("Ş", "S");
        toTranslate.put("Ö", "O");
        toTranslate.put("Ç", "C");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (toTranslate.containsKey(c)) {
                sb.append(toTranslate.get(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}