package com.emojibot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

//TODO
import javax.security.auth.login.LoginException;

public class Bot {
    private static final Dotenv envConfig = Dotenv.configure().load();
    private final ShardManager shardManager;

    /**
     * Return the token according to dev/public status from config
     * @return token to be used
     */
    public String getToken() {
        if(BotProperties.getDevMode())
            return envConfig.get("TOKEN_DEV");
        else
            return envConfig.get("TOKEN_PUBLIC");
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Bot() {
        System.out.println(getToken());
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(getToken());
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("hey!"));
        shardManager = builder.build();
    }

}
