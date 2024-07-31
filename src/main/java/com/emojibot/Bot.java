package com.emojibot;

import com.emojibot.events.CommandManager;
import com.emojibot.events.EventListener;
import com.emojibot.utils.EmojiCache;
import com.emojibot.utils.MongoManager;
import com.emojibot.utils.button_listeners.ButtonListener;
import com.emojibot.utils.command.TopggManager;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {
    private static final Dotenv config = Dotenv.configure().load();
    private ShardManager shardManager;
    private final EmojiCache emojiCache = new EmojiCache();
    private final TopggManager topggManager = new TopggManager();


    public String getToken() {
        if (BotConfig.getDevMode())
            return config.get("TOKEN_DEV");
        else
            return config.get("TOKEN_PUBLIC");
    }

    public Bot() {
        try {
            // Initialize the MongoDB connection
            MongoManager.connect();
            /*
            // Calculate the number of shards needed manually if auto is not good enough
            int totalGuilds = 96500;
            int maxGuildsPerShard = 1500; // Adjust based on your performance needs

            int numberOfShards = (int) Math.ceil((double) totalGuilds / maxGuildsPerShard);
            System.out.println("Total shard count: " + numberOfShards);
            */
            
            // Blue emoji color - 0xa7cfe2

            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(getToken())
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.watching("/start to get started with Emoji!"))
                    .setMemberCachePolicy(MemberCachePolicy.NONE)
                    .enableCache(CacheFlag.EMOJI, CacheFlag.ROLE_TAGS)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS) // Enable required intents
                    .addEventListeners(new EventListener(), new CommandManager(this), emojiCache, new ButtonListener(this));

            shardManager = builder.build();


            // Shutdown hook to disconnect from MongoDB
            Runtime.getRuntime().addShutdownHook(new Thread(MongoManager::disconnect));

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

    public TopggManager getTopggManager() {
        return topggManager;
    }

    public static void main(String[] args) {
        new Bot();
    }
}