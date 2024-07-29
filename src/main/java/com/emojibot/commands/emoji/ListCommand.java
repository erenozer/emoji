package com.emojibot.commands.emoji;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.events.ButtonListener;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.Localization;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

public class ListCommand extends EmojiCommand {

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

        this.localizedNames.put(DiscordLocale.TURKISH, "listele");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucudaki bütün emojileri listeler");

        this.description = "Lists all emojis in the server";
        this.cooldownDuration = 30;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        List<RichCustomEmoji> emojis = event.getGuild().getEmojiCache().asList();

        
        if(emojis.isEmpty()) {
            // Server has no emojis to list
            event.getHook().sendMessage(String.format(localization.getMsg("list_command", "no_emojis"), BotConfig.infoEmoji())).queue();
            return;
        }

        int pageSize = 10;
        
        // Calculate total pages
        int totalPages = (int) Math.ceil((double) emojis.size() / pageSize); 

        // Create a unique session id for the validity of the buttons
        String sessionId = ButtonListener.createUniqueId(event.getUser().getId());

        // Put the current status of the buttons in the map with sessionId as key
        currentValues.put(sessionId, new CurrentValues(1, totalPages, pageSize, emojis));

        // Show the first page initially
        showPage(event, emojis, 1, totalPages, pageSize, sessionId); 

        // Schedule expiration of the buttons after 2 minutes
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expireSession(sessionId, event);
            }
        }, Duration.ofMinutes(2).toMillis());
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

        // Get the localization for the user
        Localization localization = Localization.getLocalization(userId);

        // Create buttons based on current page
        List<ItemComponent> buttons = new ArrayList<>();
        if (totalPages >= 0) {
            String prevButtonId = sessionId + ":list:previous";
            String nextButtonId = sessionId + ":list:next";

            Button currentPageButton = Button.of(ButtonStyle.SECONDARY, "disabled", String.format(localization.getMsg("list_command", "page"), currentPage, totalPages), Emoji.fromFormatted(BotConfig.infoEmoji())).withDisabled(true);
            Button previousButton = Button.of(ButtonStyle.PRIMARY, prevButtonId, localization.getMsg("list_command", "previous_page"), Emoji.fromUnicode("⬅")).withDisabled(currentPage == 1);
            Button nextButton = Button.of(ButtonStyle.PRIMARY, nextButtonId, localization.getMsg("list_command", "next_page"), Emoji.fromUnicode("➡")).withDisabled(currentPage == totalPages);

            buttons.add(currentPageButton);
            buttons.add(previousButton);
            buttons.add(nextButton);
        }

        // Send the embed with buttons
        hook.editOriginal(emojiString.toString()).setComponents(ActionRow.of(buttons)).queue();
    }

    public static void handleClick(ButtonInteractionEvent event, boolean isNext) {
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

        int newPage = currentPage;

        if(isNext) {
            newPage++;
        } else {
            newPage--;
        }

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

        Localization localization = Localization.getLocalization(event.getUser().getId());
    
        MessageEmbed expiredEmbed = new EmbedBuilder()
                .addField(localization.getMsg("list_command", "button_expired"), localization.getMsg("list_command", "button_expired_description"), true)
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();
    

        // Remove the buttons
        hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(); 
    }
}