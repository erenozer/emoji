package com.emojibot.commands.emoji;

import java.util.List;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ListCommand extends Command {

    public ListCommand(Bot bot) {
        super(bot);
        this.name = "list";
        this.description = "Lists all emojis in the server";
    }


    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        List<RichCustomEmoji> emojis = event.getGuild().getEmojiCache().asList();

        StringBuilder emojiString = new StringBuilder();

        for (RichCustomEmoji emoji : emojis) {
            System.out.println("a");
            emojiString.append(emoji.getAsMention()).append(" ");
        }

        MessageEmbed response = new EmbedBuilder()
                .setTitle("Emoji List")
                .setDescription(emojiString.toString())
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();
        
        event.getHook().sendMessageEmbeds(response).queue();

        
    }
    
}
