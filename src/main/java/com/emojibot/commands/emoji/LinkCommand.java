package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Objects;

public class LinkCommand extends Command {
    private final EmojiCache emojiCache;
    private static final int MAX_RESULTS = 1;

    public LinkCommand(Bot bot) {
        super(bot);
        this.name = "link";
        this.description = "Get the link of an emoji";
        this.cooldownDuration = 2;

        OptionData emojiArgument = new OptionData(OptionType.STRING, "emoji", "Emoji to get the link", true);
        this.args.add(emojiArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();

        String emojiName = EmojiInput.extractEmojiName(emojiInput);
        String emojiId = EmojiInput.extractEmojiId(emojiInput);
        
        List<RichCustomEmoji> cachedEmojis = emojiCache.getEmojis(emojiName);

        // User provided the full emoji format, return the link
        if (cachedEmojis.isEmpty() && emojiId != null) {
            String url = emojiInput.contains("<a:") ?
                    String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emojiId) :
                    String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emojiId);

            event.reply(BotConfig.yesEmoji() + " Link of the emoji `" + emojiName + "`:\n" + url).queue();

        } else if (!cachedEmojis.isEmpty()) {
            // Emoji was found using the name
            StringBuilder response = new StringBuilder();
            response.append(BotConfig.yesEmoji() + " Link of the emoji \"").append(emojiName).append("\":\n");

            int count = 0;
            for (RichCustomEmoji emoji : cachedEmojis) {
                if (count >= MAX_RESULTS) {
                    break;
                }

                String url = emoji.isAnimated() ?
                        String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emoji.getId()) :
                        String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emoji.getId());
                response.append(url).append("\n");

                count++;
            }

            event.reply(response.toString()).queue();
        } else {
            event.reply(BotConfig.noEmoji() + " I couldn't find the emoji you asked for. Please make sure it is a valid **custom emoji**.").setEphemeral(true).queue();
        }
    }
}