package com.emojibot.commands.emoji;

import java.util.ArrayList;
import java.util.List;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;


public class ListCommand extends Command {

    public ListCommand(Bot bot) {
        super(bot);
        this.name = "list";
        this.description = "Lists all emojis in the server with pagination";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        List<RichCustomEmoji> emojis = event.getGuild().getEmojiCache().asList();

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) emojis.size() / pageSize); // Calculate total pages

        showPage(event, emojis, 1, totalPages); // Show the first page initially
    }

    private void showPage(SlashCommandInteractionEvent event, List<RichCustomEmoji> emojis, int currentPage, int totalPages) {
        int startIndex = (currentPage - 1) * 10;
        int endIndex = Math.min(startIndex + 10, emojis.size());

        StringBuilder emojiString = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            emojiString.append(emojis.get(i).getAsMention()).append(" ");
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Emoji List - Page " + currentPage + "/" + totalPages)
                .setDescription(emojiString.toString())
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();

                
        // Create buttons based on current page
        List<ItemComponent> buttons = new ArrayList<>();
        if (totalPages > 1) {
            Button previousButton = Button.of(ButtonStyle.PRIMARY, "previous", "Previous Page", Emoji.fromFormatted("⬅"));
            Button nextButton = Button.of(ButtonStyle.PRIMARY, "next", "Next Page", Emoji.fromFormatted("➡"));
            
            buttons.add(previousButton);
            buttons.add(nextButton);
            

            if (currentPage == 1) {
                //buttons.remove(previousButton);
            } else if (currentPage == totalPages) {
                buttons.remove(nextButton); 
            }
        }

        // Send the embed with buttons
        event.getHook().sendMessageEmbeds(embed).setActionRow(buttons).queue();
    }

    
    
}