package com.emojibot.commands.emoji;

import com.emojibot.EmojiCache;
import com.emojibot.commands.Command;
import com.emojibot.Bot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;
import java.util.stream.Collectors;

public class SearchCommand extends Command {
    private final EmojiCache emojiCache;

    public SearchCommand(Bot bot) {
        super(bot);
        this.name = "search";
        this.description = "Search for specific emojis.";

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "name", "Emoji name to be searched", true);
        this.args.add(emojiNameArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String emojiName = normalize(event.getOption("name").getAsString());
        List<RichCustomEmoji> emojiList = emojiCache.getEmojis(emojiName);

        if (emojiList != null && !emojiList.isEmpty()) {
            StringBuilder replyMessage = new StringBuilder("Found emojis: ");
            for (RichCustomEmoji emoji : emojiList) {
                replyMessage.append(emoji.getAsMention()).append(" ");
            }
            event.reply(replyMessage.toString()).queue();
        } else {
            // Try a fuzzy search if no exact match found - GPT methodu
            List<RichCustomEmoji> similarEmojis = searchSimilarEmojis(emojiName);
            if (!similarEmojis.isEmpty()) {
                StringBuilder replyMessage = new StringBuilder("No exact matches found. Here are some similar results: ");
                for (RichCustomEmoji emoji : similarEmojis) {
                    replyMessage.append(emoji.getAsMention()).append(" ");
                }
                event.reply(replyMessage.toString()).queue();
            } else {
                event.reply("I couldn't find similar or exact matches to your search. :(").queue();
            }
        }
    }

    private String normalize(String input) {
        return input.trim().toLowerCase().replaceAll("\\s+", "");
    }

    private List<RichCustomEmoji> searchSimilarEmojis(String emojiName) {
        return emojiCache.getAllEmojis().stream()
                .filter(emoji -> levenshteinDistance(emojiName, normalize(emoji.getName())) <= 2)
                .collect(Collectors.toList());
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}