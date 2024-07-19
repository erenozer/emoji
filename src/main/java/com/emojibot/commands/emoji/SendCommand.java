package com.emojibot.commands.emoji;

import java.util.Objects;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;


public class SendCommand extends Command {

    public SendCommand(Bot bot) {
        super(bot);
        this.name = "send";
        this.description = "Send any emoji that is in this server using it's name only!";
        this.args.add(new OptionData(OptionType.STRING, "name", "Name of an emoji that is in this server", true));
        this.cooldownDuration = 4;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String emojiInput = Objects.requireNonNull(event.getOption("name")).getAsString();

        String emojiName = (EmojiInput.extractEmojiName(emojiInput));
        
        // Emoji is not provided directly, search for it in the server using the name
        var emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        if(emoji == null) {
            event.getHook().sendMessage(String.format("%s I can't find the emoji with the name you specified, the emoji must be from this server.", BotConfig.noEmoji())).queue();
            return;
        }

        event.getHook().sendMessage(emoji.getAsMention()).queue();

    }
}
