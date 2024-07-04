package com.emojibot.events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionDisconnectEvent;
import net.dv8tion.jda.api.events.session.SessionInvalidateEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        System.out.println("Shard " + shardId+1 + " logged in as " + event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        System.out.println("Shard " + shardId + " is attempting to reconnect");
    }

    @Override
    public void onSessionInvalidate(SessionInvalidateEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        System.out.println("Shard " + shardId + " has invalidated its session completely");
    }

    @Override
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        int shardId = event.getJDA().getShardInfo().getShardId();
        System.out.println("Shard " + shardId + " disconnected from the gateway, code: " + event.getCloseCode());
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
}