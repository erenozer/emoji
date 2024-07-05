package com.emojibot.events;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionInvalidateEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Logged in as {}", event.getJDA().getSelfUser().getName());
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Attempting to reconnect...");
    }

    @Override
    public void onSessionInvalidate(SessionInvalidateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Invalidated session completely.");
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.warn("Disconnected from the gateway, code: {}", event.getCloseCode());
    }

    /*
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }
    }
    */

    /**
     * event.getState().name returns all capitalized event name, this generates a better event name for the logger
     * @param str event string
     * @return better formatted event name
     */
    private static String generateEventName(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase() + "Event";
    }
}