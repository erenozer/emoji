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
    private final Map<String, List<RichCustomEmoji>> emojiCache = new HashMap<>();

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        System.out.println("Caching emojis for guild " + event.getGuild().getName());
        cacheEmojis(event.getGuild());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        cacheEmojis(event.getGuild());
    }

    /**
     * Cache emojis from a guild
     * @param guild The guild to cache emojis from
     */
    private void cacheEmojis(Guild guild) {
        for (RichCustomEmoji emoji : guild.getEmojis()) {
            emojiCache.computeIfAbsent(emoji.getName(), k -> new ArrayList<>()).add(emoji);
        }
    }

    public List<RichCustomEmoji> getEmojis(String emojiName) {
        return emojiCache.getOrDefault(emojiName, new ArrayList<>());
    }

    public void clearCache() {
        emojiCache.clear();
    }
}