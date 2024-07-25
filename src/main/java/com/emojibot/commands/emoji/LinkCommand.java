package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import com.emojibot.commands.utils.language.Localization;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public class LinkCommand extends Command {
    private final EmojiCache emojiCache;

    public LinkCommand(Bot bot) {
        super(bot);
        this.name = "link";
        this.description = "Get the image link of an emoji";
        this.cooldownDuration = 2;

        OptionData emojiArgument = new OptionData(OptionType.STRING, "emoji", "Name of the emoji or the emoji itself", true);
        this.args.add(emojiArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String emojiName = EmojiInput.extractEmojiName(emojiInput);
        String emojiId = EmojiInput.extractEmojiId(emojiInput);
        
        RichCustomEmoji emojiFromCache = emojiCache.getEmojis(emojiName).stream().findAny().orElse(null);

        // User provided the full emoji format, return the link
        if (emojiFromCache == null && emojiId != null) {
            String url = emojiInput.contains("<a:") ?
                    String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emojiId) :
                    String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emojiId);

            event.reply(String.format("%s %s: %s", BotConfig.yesEmoji(), localization.getMsg("link_command", "link_of_emoji"), url)).queue();

        } else if (emojiFromCache != null) {
            // Emoji was found using the name, from the cache
            String url = emojiFromCache.isAnimated() ?
                String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emojiFromCache.getId()) :
                String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emojiFromCache.getId());
  
            event.reply(String.format("%s %s: %s", BotConfig.yesEmoji(), localization.getMsg("link_command", "link_of_emoji"), url)).queue();
            
        } else {
            event.reply(String.format(localization.getMsg("link_command", "emoji_not_found"), BotConfig.noEmoji())).setEphemeral(true).queue();
        }
    }
}