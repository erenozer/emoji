package com.emojibot.events;

import com.emojibot.Bot;
import com.emojibot.commands.emoji.ListCommand;
import com.emojibot.commands.utils.UsageTerms;
import com.emojibot.commands.utils.language.LanguageManager;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ButtonListener extends ListenerAdapter {
    private final Map<String, Consumer<ButtonInteractionEvent>> buttonHandlers = new HashMap<>();

    /**
     * Initialize all possible button actions
     * @param bot
     */
    public ButtonListener(Bot bot) {
        // List command next/previous page buttons
        registerButtonHandler("list:previous", event -> {
            ListCommand.handlePrevious(event);
        });
        registerButtonHandler("list:next", event -> {
            ListCommand.handleNext(event);
        });

        // Usage terms accept/decline buttons
        registerButtonHandler("accept_terms", event -> {
            boolean result = UsageTerms.setUserStatus(event.getUser().getId(), true);

            UsageTerms.handleClick(event, result, true);
        });
        registerButtonHandler("decline_terms", event -> {
            boolean result = UsageTerms.setUserStatus(event.getUser().getId(), false);

            UsageTerms.handleClick(event, result, false);
        });

        // Language selection buttons
        registerButtonHandler("language:en", event -> {
            boolean result = LanguageManager.setUserLanguage(event.getUser().getId(), "en");
            
            LanguageManager.handleClick(event, result, "en");
        });
        registerButtonHandler("language:tr", event -> {
            boolean result = LanguageManager.setUserLanguage(event.getUser().getId(), "tr");

            LanguageManager.handleClick(event, result, "tr");
        });

    }

    /**
     * Maps the created button handlers into a map with ID -> method
     * @param buttonId 
     * @param handler
     */
    public void registerButtonHandler(String buttonId, Consumer<ButtonInteractionEvent> handler) {
        buttonHandlers.put(buttonId, handler);
    }

    /**
     * Handles the button interaction with user validation
     * ! While using this logic, make sure to save session IDs in the command and validate them 
     * @param event 
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] contents = event.getComponentId().split(":");
        int length = contents.length;

        if(length != 4 && length != 3) {
            throw new IllegalArgumentException("Button ID is not valid!");
        }

        System.out.println("Button pressed: " + event.getComponentId());

        if(!contents[1].equals(event.getUser().getId())) {
            // Ignore other users' button presses
            return;
        }

        // Usage warning buttons do not include a command name, if that is the case, handle accordingly
        String actionName = length == 4 ? String.format("%s:%s", contents[2], contents[3]) : contents[2];

        buttonHandlers.get(actionName).accept(event);
    }

    /**
     * Returns a unique ID with the format uuid:userId
     * @param userId user Id to create a unique session id from
     * @return String in the format uuid:userId
     */
    public static String createUniqueId(String userId) {
        StringBuilder id = new StringBuilder();
        String uuid = UUID.randomUUID().toString();

        id.append(uuid);    
        id.append(":");
        id.append(userId);

        return id.toString();
    }

}