package com.emojibot.commands.emoji;

import com.emojibot.commands.Command;
import com.emojibot.Bot;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Objects;

public class LinkCommand extends Command {
    private final EmojiCache emojiCache;
    private static final int MAX_RESULTS = 5;

    public LinkCommand(Bot bot) {
        super(bot);
        this.name = "link";
        this.description = "Get the link of an emoji";

        OptionData emojiArgument = new OptionData(OptionType.STRING, "emoji", "Emoji to get the link", true);
        this.args.add(emojiArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();

        String emojiName = EmojiInput.extractEmojiName(emojiInput);
        
        List<RichCustomEmoji> cachedEmojis = emojiCache.getEmojis(emojiName);

        if (cachedEmojis.isEmpty()) {
            event.reply("No custom emoji found with that name.").queue();
        } else {
            StringBuilder response = new StringBuilder();
            response.append("Links for the emoji `").append(emojiName).append("`:\n");

            int count = 0;
            for (RichCustomEmoji emoji : cachedEmojis) {
                if (count >= MAX_RESULTS) {
                    break;
                }

                String url = emoji.isAnimated() ?
                        String.format("https://cdn.discordapp.com/emojis/%s.gif?v=1", emoji.getId()) :
                        String.format("https://cdn.discordapp.com/emojis/%s.png", emoji.getId());
                response.append(url).append("\n");

                count++;
            }

            event.reply(response.toString()).queue();
        }
    }
}