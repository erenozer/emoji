package com.emojibot.utils.command;

import org.discordbots.api.client.DiscordBotListAPI;

import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;

import club.minnced.discord.webhook.WebhookClient;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TopggManager {
    private static final Dotenv config = Dotenv.configure().load();

    private static final WebhookClient logHook = WebhookClient.withUrl(config.get("URL_LOGS_WEBHOOK"));
    
    public DiscordBotListAPI api;

    public TopggManager() {
        api = new DiscordBotListAPI.Builder()
                .token(config.get("TOPGG_TOKEN"))
                .botId("414878659267133445")
                .build();
    }

    public static void sendVoteEmbed(InteractionHook hook, Localization localization) {
        MessageEmbed voteEmbed = new EmbedBuilder()
                .setAuthor(localization.getMsg("vote_embed", "title"), "https://top.gg/bot/emoji/vote")
                .setDescription(localization.getMsg("vote_embed", "desc"))
                .setFooter(localization.getMsg("vote_embed", "footer"), "https://cdn.discordapp.com/emojis/727948775884324925.webp?size=240&quality=lossless")
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();

        hook.sendMessageEmbeds(voteEmbed).queue();
    }

    public boolean hasVoted(String userId) {
        // Use an AtomicBoolean to store the result of the vote check
        AtomicBoolean voted = new AtomicBoolean(false);

        // Use CompletableFuture to handle the asynchronous call to the API
        CompletableFuture<Boolean> future = api.hasVoted(userId).toCompletableFuture();

        try {
            future.whenComplete((hasVoted, err) -> {
                if (err != null) {
                    // If an error occurred, log it and return true to prevent issues with the command
                    logHook.send(":warning: Error while checking vote status for the user: " + userId + "\n" + err.getMessage());
                    voted.set(true);
                }

                // If the vote check was successful, set the voted to the result
                voted.set(hasVoted != null && hasVoted);
                
            }).get(); // Block and wait for the result
        } catch (InterruptedException | ExecutionException e) {
            // If an error occurred, log it and return true to prevent issues with the command
            logHook.send(":warning: Error while checking vote status for the user: " + userId + "\n" + e.getMessage());
            voted.set(true);
        }

        return voted.get();
    }

    /*
     ! This method does not work unless compiled with gradle run/build. 
     */
    public CompletableFuture<Boolean> updateStats(int guildCount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
    
        api.setStats(guildCount).whenComplete((response, err) -> {
            if (err != null) {
                logHook.send(":warning: Error while updating bot stats on top.gg\n" + err.getMessage());
                future.complete(false); 
            } else {
                logHook.send(":information_source: Successfully updated bot stats on top.gg.");
                future.complete(true);
            }
        });
    
        return future;
    }
    
    public DiscordBotListAPI getTopggApi() {
        return api;
    }

}
