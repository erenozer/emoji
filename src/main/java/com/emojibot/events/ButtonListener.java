package com.emojibot.events;

import com.emojibot.Bot;
import com.emojibot.commands.emoji.ListCommand;
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
        // Register command handlers
        registerButtonHandler("list:previous", event -> {
            ListCommand.handlePrevious(event);
        });
        registerButtonHandler("list:next", event -> {
            ListCommand.handleNext(event);
        });
    }

    /**
     * Puts the name of the action and action to a map
     */
    public void registerButtonHandler(String buttonId, Consumer<ButtonInteractionEvent> handler) {
        buttonHandlers.put(buttonId, handler);
    }

    /**
     * Handles the button interaction with user validation
     * ! While using this logic, make sure to save session IDs in the command and validate them 
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] contents = event.getComponentId().split(":");
        
        if(contents.length != 4) {
            throw new IllegalArgumentException("Button ID is not valid!");
        }

        System.out.println("Button pressed: " + event.getComponentId());

        if(!contents[1].equals(event.getUser().getId())) {
            // Ignore other users' button presses
            return;
        }


        String actionName = String.format("%s:%s", contents[2],contents[3]);
        buttonHandlers.get(actionName).accept(event);
    }

    public static String createUniqueId(String userId) {
        StringBuilder id = new StringBuilder();
        String uuid = UUID.randomUUID().toString();

        id.append(uuid);    
        id.append(":");
        id.append(userId);

        return id.toString();
    }
}