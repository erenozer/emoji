package com.emojibot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class EmojiCache extends ListenerAdapter {
    // Save the emojis in a map with the emoji NAME as the key
    private final Map<String, List<RichCustomEmoji>> emojiCache = new HashMap<>();

    // Save all the cached emojis in a list
    private List<RichCustomEmoji> allEmojis = new ArrayList<>();

    // Save the hidden guilds in a hash set, guild ids are unique
    private static final Set<String> hiddenGuilds = new HashSet<>();

    // Use a static block to load the hidden guilds when the class is loaded
    // (instead of using a constructor, which may be called multiple times)
    static {
        loadHiddenGuilds();
    }

    /**
     * Load the hidden guilds into a set when the class is loaded
     */
    private static void loadHiddenGuilds() {
        MongoCollection<Document> collection = MongoManager.getServersCollection();
        if (collection != null) {
            // Get an iterator starting from the first document in the servers collection where hidden is true
            try (MongoCursor<Document> it = collection.find(new Document("hidden", true)).iterator()) {
                while (it.hasNext()) {
                    Document doc = it.next();
                    hiddenGuilds.add(doc.getString("server_id"));
                }
            }
        }
    }

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
     * Cache emojis from a guild, unless the guild is in the hidden guilds set
     * @param guild The guild to cache emojis from
     */
    private void cacheEmojis(Guild guild) {
        String guildId = guild.getId();

        // Check if the server's emojis should be hidden
        if (hiddenGuilds.contains(guildId)) {
            return; // Do not cache emojis from this guild
        }

        // Cache the emojis from the guild since they are not hidden
        for (RichCustomEmoji emoji : guild.getEmojiCache()) {
            emojiCache.computeIfAbsent(emoji.getName(), k -> new ArrayList<>()).add(emoji);
            allEmojis.add(emoji);
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
        return allEmojis;
    }

    public List<RichCustomEmoji> getRandomEmojis(int count) {
        List<RichCustomEmoji> randomEmojis = new ArrayList<>(count);
        Random random = new Random();
        int size = allEmojis.size();

        for (int i = 0; i < count; i++) {
            randomEmojis.add(allEmojis.get(random.nextInt(size)));
        }

        return randomEmojis;
    }

}