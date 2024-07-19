package com.emojibot.commands.emoji;

import java.util.Objects;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;


public class JumboCommand extends Command {
    private EmojiCache emojiCache;

    public JumboCommand(Bot bot) {
        super(bot);
        this.name = "jumbo";
        this.description = "Get your emoji as an image, JUMBO version!";
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Name of the emoji or the emoji itself", true));
        this.cooldownDuration = 4;
        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();

        String emojiName = (EmojiInput.extractEmojiName(emojiInput));
        String emojiId = EmojiInput.extractEmojiId(emojiInput);

        String url = null;
        
        if(emojiId != null) {
            // Emoji is provided directly, use the ID to get the image
            url = emojiInput.contains("<a:") ?
                    String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emojiId) :
                    String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emojiId);

        } else {
            // Emoji is not provided directly, search for it in the server using the name
            var emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

            // If not found in the server, search for the emoji in the bot's cache 
            if(emoji == null) {
                emoji = emojiCache.getEmojis(emojiName).stream().findFirst().orElse(null);
            }

            // If emoji is found using either method, get the image URL
            if(emoji != null) {
                url = emoji.getImageUrl();
            } 

        }

        if(url == null) {
            event.getHook().sendMessage(String.format("%s I can't find the emoji, please send the emoji itself or the name of it.", BotConfig.noEmoji())).queue();
            return;
        }

        MessageEmbed jumboEmbed = new EmbedBuilder()
        .setImage(url)
        .setColor(BotConfig.getGeneralEmbedColor())
        .build();

        event.getHook().sendMessageEmbeds(jumboEmbed).queue();

    }
}
