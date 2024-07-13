package com.emojibot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmojiCache extends ListenerAdapter {
    // Save the emojis in a map with the emoji NAME as the key
    private final Map<String, List<RichCustomEmoji>> emojiCache = new HashMap<>();

    /**
     * Cache emojis when a guild is ready (bot reconnects)
     */
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        cacheEmojis(event.getGuild());
    }

    /**
     * Cache emojis when the bot joins a guild
     */
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        cacheEmojis(event.getGuild());
    }

    /**
     * Cache emojis from a guild
     * @param guild The guild to cache emojis from
     */
    private void cacheEmojis(Guild guild) {
        for (RichCustomEmoji emoji : guild.getEmojiCache()) {
            emojiCache.computeIfAbsent(emoji.getName(), k -> new ArrayList<>()).add(emoji);
        }
    }

    /**
     * Get the same named emojis from cache
     * @param emojiName The name of the emoji
     * @return A list of emojis with the same name
     */
    public List<RichCustomEmoji> getEmojis(String emojiName) {
        return emojiCache.getOrDefault(emojiName, new ArrayList<>());
    }

    /**
     * Get all emojis from cache as a list
     * @return A list of all emojis
     */
    public List<RichCustomEmoji> getAllEmojis() {
        List<RichCustomEmoji> allEmojis = new ArrayList<>();
        for (List<RichCustomEmoji> emojis : emojiCache.values()) {
            allEmojis.addAll(emojis);
        }
        return allEmojis;
    }

}