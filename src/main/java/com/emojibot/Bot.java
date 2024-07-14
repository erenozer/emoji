package com.emojibot;

import com.emojibot.events.ButtonListener;
import com.emojibot.events.CommandManager;
import com.emojibot.events.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.Cache;

public class Bot {
    private static final Dotenv config = Dotenv.configure().load();
    private ShardManager shardManager;
    private final EmojiCache emojiCache = new EmojiCache();

    public String getToken() {
        if (BotConfig.getDevMode())
            return config.get("TOKEN_DEV");
        else
            return config.get("TOKEN_PUBLIC");
    }

    public Bot() {
        try {

            /*
            // Calculate the number of shards needed manually if auto is not good enough
            int totalGuilds = 96500;
            int maxGuildsPerShard = 1500; // Adjust based on your performance needs

            int numberOfShards = (int) Math.ceil((double) totalGuilds / maxGuildsPerShard);
            System.out.println("Total shard count: " + numberOfShards);
            */
            
            // Blue emoji color - 0xa7cfe2

            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(getToken())
                    .setStatus(OnlineStatus.IDLE)
                    .setActivity(Activity.watching("/start to get started with Emoji!"))
                    .setMemberCachePolicy(MemberCachePolicy.NONE)
                    .enableCache(CacheFlag.EMOJI, CacheFlag.ROLE_TAGS)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS) // Enable required intents
                    .addEventListeners(new EventListener(), new CommandManager(this), emojiCache, new ButtonListener(this));

            shardManager = builder.build();

        } catch (InvalidTokenException e) {
            System.err.println("Invalid token provided. Please check your token and try again.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the bot", e);
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Dotenv getConfig() {
        return config;
    }

    public EmojiCache getEmojiCache() {
        return emojiCache;
    }

    public static void main(String[] args) {
        new Bot();
    }
}