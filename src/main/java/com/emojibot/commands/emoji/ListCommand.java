package com.emojibot.commands.emoji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import com.emojibot.events.ButtonListener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

public class ListCommand extends Command {

    private static class CurrentValues {
        private int currentPage;
        private int totalPages;
        private int pageSize;
        private List<RichCustomEmoji> emojis;

        public CurrentValues(int currentPage, int totalPages, int pageSize, List<RichCustomEmoji> emojis) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.pageSize = pageSize;
            this.emojis = emojis;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getPageSize() {
            return pageSize;
        }

        public List<RichCustomEmoji> getEmojis() {
            return emojis;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
    }

    // Holds UUID:USERID as key and Current Page as value
    private static HashMap<String, CurrentValues> currentValues = new HashMap<>();

    public ListCommand(Bot bot) {
        super(bot);
        this.name = "list";
        this.description = "Lists all emojis in the server with pagination";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        List<RichCustomEmoji> emojis = event.getGuild().getEmojiCache().asList();

        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) emojis.size() / pageSize); // Calculate total pages

        String sessionId = ButtonListener.createUniqueId(event.getUser().getId());
        currentValues.put(sessionId, new CurrentValues(1, totalPages, pageSize, emojis));

        showPage(event, emojis, 1, totalPages, pageSize, sessionId); // Show the first page initially
    }

    private static void showPage(SlashCommandInteractionEvent event, List<RichCustomEmoji> emojis, int currentPage, int totalPages, int pageSize, String sessionId) {
        showPage(event.getHook(), event.getUser().getId(), emojis, currentPage, totalPages, pageSize, sessionId);
    }

    private static void showPage(ButtonInteractionEvent event, List<RichCustomEmoji> emojis, int currentPage, int totalPages, int pageSize, String sessionId) {
        showPage(event.getHook(), event.getUser().getId(), emojis, currentPage, totalPages, pageSize, sessionId);
    }

    private static void showPage(InteractionHook hook, String userId, List<RichCustomEmoji> emojis, int currentPage, int totalPages, int pageSize, String sessionId) {
        // Edit the message to show the given currentPage

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, emojis.size());

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
            String prevButtonId = sessionId + ":list:previous";
            String nextButtonId = sessionId + ":list:next";

            Button previousButton = Button.of(ButtonStyle.PRIMARY, prevButtonId, "Previous Page", Emoji.fromUnicode("⬅")).withDisabled(currentPage == 1);
            Button nextButton = Button.of(ButtonStyle.PRIMARY, nextButtonId, "Next Page", Emoji.fromUnicode("➡")).withDisabled(currentPage == totalPages);

            buttons.add(previousButton);
            buttons.add(nextButton);
        }

        
        // Send the embed with buttons
        hook.editOriginalEmbeds(embed).setActionRow(buttons).queue();
    }

    public static void handleNext(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String[] contents = event.getComponentId().split(":");

        // get the UUID:UserId value to get the current page
        String uniqueID = String.format("%s:%s", contents[0], contents[1]);

        if (!currentValues.containsKey(uniqueID)) {
            // Older button, ignore
            return;
        }

        CurrentValues values = currentValues.get(uniqueID);
        int currentPage = values.getCurrentPage();
        int totalPages = values.getTotalPages();
        int pageSize = values.getPageSize();
        List<RichCustomEmoji> emojis = values.getEmojis();

        int newPage = currentPage + 1;
        values.setCurrentPage(newPage);
        showPage(event, emojis, newPage, totalPages, pageSize, uniqueID);
    }

    public static void handlePrevious(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String[] contents = event.getComponentId().split(":");
        // get the UUID:UserId value to get the current state
        String uniqueID = String.format("%s:%s", contents[0], contents[1]);

        if (!currentValues.containsKey(uniqueID)) {
            // Old button, ignore
            return;
        }
        
        CurrentValues values = currentValues.get(uniqueID);
        int currentPage = values.getCurrentPage();
        int totalPages = values.getTotalPages();
        int pageSize = values.getPageSize();
        List<RichCustomEmoji> emojis = values.getEmojis();

        int newPage = currentPage - 1;
        values.setCurrentPage(newPage);

        showPage(event, emojis, newPage, totalPages, pageSize, uniqueID);
    }
}