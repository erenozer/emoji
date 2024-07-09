package com.emojibot.commands.emoji;

import com.emojibot.EmojiCache;
import com.emojibot.commands.Command;
import com.emojibot.commands.utils.EmbedCreator;
import com.emojibot.commands.utils.EmojiInput;
import com.emojibot.Bot;
import com.emojibot.BotConfig;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchCommand extends Command {
    private final EmojiCache emojiCache;

    public SearchCommand(Bot bot) {
        super(bot);
        this.name = "search";
        this.description = "Search for specific emojis";

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "emojiname", "Emoji name to be searched", true, false);
        this.args.add(emojiNameArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        // Defer the reply to avoid the 3 second timeout while searching, will use hooks to reply later
        event.deferReply().queue();

        String emojiInput = EmojiInput.normalize(Objects.requireNonNull(event.getOption("emojiname")).getAsString());

        String emojiName = EmojiInput.extractEmojiName(emojiInput);
        
        List<RichCustomEmoji> emojiList = emojiCache.getEmojis(emojiName);

        // getEmojis returns a list of emojis that match the search query
        // that list will not be null, if no emojis are found, the list will be empty
        if (!emojiList.isEmpty() && emojiList.size() > 15) {
            replyWithFoundEmojis(event, emojiList);
        } else {
            // Try a fuzzy search if less than 15 emojis are found/not found
            List<RichCustomEmoji> similarEmojis = searchSimilarEmojis(emojiName);
            if (similarEmojis.size() > emojiList.size()) {
                // Reply with the similar emojis if they are more than the exact matches
                replyWithFoundEmojis(event, similarEmojis);
                event.getHook().sendMessage(BotConfig.infoEmoji() + " Similar named emojis were added!").setEphemeral(true).queue();
            } else if(!emojiList.isEmpty()) { 
                // Reply with the found emojis if there are any
                replyWithFoundEmojis(event, emojiList);
            } else {
                // No similar or exact matches found
                event.getHook().sendMessage("I couldn't find similar or exact matches to your search. :(").queue();
            }
        }
    }

    private void replyWithFoundEmojis(SlashCommandInteractionEvent event, List<RichCustomEmoji> emojiList) {
        // Create a reply message with the found emojis and send it
        StringBuilder replyMessage = new StringBuilder();
        int count = 0;
        for (RichCustomEmoji emoji : emojiList) {
            if (count >= 25) {
                break;
            }
            replyMessage.append(emoji.getAsMention()).append(" ");
            count++;
        }
        event.getHook().sendMessage(replyMessage.toString()).queue();
    }

    private List<RichCustomEmoji> searchSimilarEmojis(String emojiName) {
        // Search for emojis with a Levenshtein distance of 2 or less
        return emojiCache.getAllEmojis().stream()
                .filter(emoji -> levenshteinDistance(emojiName, EmojiInput.normalize(emoji.getName())) <= 2)
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