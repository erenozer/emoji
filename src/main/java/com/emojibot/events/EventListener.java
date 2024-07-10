package com.emojibot.events;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionInvalidateEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.minnced.discord.webhook.WebhookClient;
import io.github.cdimascio.dotenv.Dotenv;

public class EventListener extends ListenerAdapter {
    private static final Dotenv config = Dotenv.configure().load();
    private static final WebhookClient shardHook = WebhookClient.withUrl(config.get("URL_SHARDS_WEBHOOK"));


    /**
     * Logs when the bot is ready
     * @param event
     */
    @Override
    public void onReady(ReadyEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Logged in as {}", event.getJDA().getSelfUser().getName());
        shardHook.send(String.format("<:oveonline:565332757706702849> Shard **%d** is ready! (%s)", shardId, event.getJDA().getSelfUser().getName()));
    }

    /**
     * Logs when the session is recreated
     * @param event SessionRecreateEvent
     */
    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Attempting to reconnect...");
        shardHook.send(String.format("<:oveidle:565330022823624714> Shard **%d** is attempting to reconnect... (%s)", shardId, event.getJDA().getSelfUser().getName()));
    }

    /**
     * Logs when the session is invalidated
     * @param event session invalidate event
     */
    @Override
    public void onSessionInvalidate(SessionInvalidateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.info("Invalidated session completely.");
        shardHook.send(String.format("<:oveoffline:565330022823624714> Shard **%d** has been invalidated completely. (%s) :warning:", shardId, event.getJDA().getSelfUser().getName()));
    }

    /**
     * Logs when the session is disconnected
     * @param event
     */
    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        Logger logger = LoggerFactory.getLogger(generateEventName(event.getState().name()));
        logger.warn("Disconnected from the gateway, code: {}", event.getCloseCode());
        shardHook.send(String.format("<:oveoffline:565330022823624714> Shard **%d** has been disconnected from the gateway. (%s)", shardId, event.getJDA().getSelfUser().getName()));

    }

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