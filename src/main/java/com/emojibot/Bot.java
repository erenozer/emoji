package com.emojibot;

import com.emojibot.events.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

//TODO
import javax.security.auth.login.LoginException;

public class Bot {
    private static final Dotenv config = Dotenv.configure().load();
    private final ShardManager shardManager;

    /**
     * Return the token according to dev/public status from config
     *
     * @return token to be used
     */
    public String getToken() {
        if (BotProperties.getDevMode())
            return config.get("TOKEN_DEV");
        else
            return config.get("TOKEN_PUBLIC");
    }

    public Bot() {
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(getToken());

        /*
        // Calculate the number of shards needed manually if auto is not good enough
        int totalGuilds = 96500;
        int maxGuildsPerShard = 1500; // Adjust based on your performance needs

        int numberOfShards = (int) Math.ceil((double) totalGuilds / maxGuildsPerShard);
        System.out.println("Total shard count: " + numberOfShards);
        */

        shardManager = builder
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("/start to get started"))
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                //.setShardsTotal(numberOfShards)
                .addEventListeners(new EventListener())
                //.enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

    }


    public ShardManager getShardManager() {
        return shardManager;
    }

    public Dotenv getConfig() {
        return config;
    }

}
