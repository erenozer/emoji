package com.emojibot.commands.emoji;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final Map<String, CurrentValues> currentValues = new HashMap<>();
    private static final Timer timer = new Timer(); // Timer for session expiration


    public ListCommand(Bot bot) {
        super(bot);
        this.name = "list";
        this.description = "Lists all emojis in the server with pages";
        this.cooldownDuration = 30;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        List<RichCustomEmoji> emojis = event.getGuild().getEmojiCache().asList();

        if(emojis.isEmpty()) {
            event.getHook().sendMessage(String.format("%s There are no emojis in the server. :^)", BotConfig.infoEmoji())).queue();
            return;
        }

        int pageSize = 10;
        
        // Calculate total pages
        int totalPages = (int) Math.ceil((double) emojis.size() / pageSize); 

        String sessionId = ButtonListener.createUniqueId(event.getUser().getId());
        currentValues.put(sessionId, new CurrentValues(1, totalPages, pageSize, emojis));

        // Show the first page initially
        showPage(event, emojis, 1, totalPages, pageSize, sessionId); 

        // Schedule expiration of the buttons after 3 minutes
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expireSession(sessionId, event);
            }
        }, Duration.ofMinutes(3).toMillis());
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

        // Create buttons based on current page
        List<ItemComponent> buttons = new ArrayList<>();
        if (totalPages >= 0) {
            String prevButtonId = sessionId + ":list:previous";
            String nextButtonId = sessionId + ":list:next";

            Button currentPageButton = Button.of(ButtonStyle.SECONDARY, "disabled", String.format("Page %d/%d", currentPage, totalPages), Emoji.fromFormatted(BotConfig.infoEmoji())).withDisabled(true);
            Button previousButton = Button.of(ButtonStyle.PRIMARY, prevButtonId, "Previous Page", Emoji.fromUnicode("⬅")).withDisabled(currentPage == 1);
            Button nextButton = Button.of(ButtonStyle.PRIMARY, nextButtonId, "Next Page", Emoji.fromUnicode("➡")).withDisabled(currentPage == totalPages);

            buttons.add(currentPageButton);
            buttons.add(previousButton);
            buttons.add(nextButton);
        }

        // Send the embed with buttons
        hook.editOriginal(emojiString.toString()).setComponents(ActionRow.of(buttons)).queue();
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

    private static void expireSession(String sessionId, SlashCommandInteractionEvent event) {
        CurrentValues values = currentValues.remove(sessionId); // Remove session data and get values
    
        if (values == null) {
            return; // Session not found, nothing to expire
        }
    
        // Retrieve interaction hook and update embed with expired message
        InteractionHook hook = event.getHook();
        if (hook == null) {
            return; // Interaction hook not available
        }
    
        MessageEmbed expiredEmbed = new EmbedBuilder()
                .addField("Command Expired", "You can run the command again with /list", true)
                .setColor(Color.RED)
                .build();
    

        // Remove the buttons
        hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(); 
    }
}