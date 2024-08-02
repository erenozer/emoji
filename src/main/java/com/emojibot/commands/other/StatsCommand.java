package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;

public class StatsCommand extends EmojiCommand {

    public StatsCommand(Bot bot) {
        super(bot);
        this.name = "stats";
        this.description = "Shows the statistics of the bot";
        this.cooldownDuration = 6;

        this.localizedNames.put(DiscordLocale.TURKISH, "istatistik");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Botun istatistiklerini gösterir");
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        Localization localization = Localization.getLocalization(event.getUser().getId());

        // Calculate latecies
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        // Calculate the total guild count across all shards
        int totalGuildCount = 0;
        for (JDA shard : bot.getShardManager().getShards()) {
            totalGuildCount += shard.getGuilds().size();
        }

        // Calculate & format uptime
        String uptimeFormatted = getFormattedUptime(localization);

        // Create and send the stats embed
        MessageEmbed statsEmbed = new EmbedBuilder()
            .setAuthor(localization.getMsg("stats_command", "title"), null, event.getJDA().getSelfUser().getAvatarUrl())
            .addField(localization.getMsg("stats_command", "guilds"), String.valueOf(totalGuildCount), true)
            .addField(localization.getMsg("stats_command", "shards"), String.valueOf(bot.getShardManager().getShardsTotal()), true)
            .addField(localization.getMsg("stats_command", "ping"), String.format("%sms | %sms", gatewayPing, restPing), true)
            .addField(localization.getMsg("stats_command", "uptime"), uptimeFormatted, true)
            .setColor(BotConfig.getGeneralEmbedColor())
            .build();

        event.replyEmbeds(statsEmbed).queue();

        // Afterwards, update the stats on top.gg if user is an admin & not in dev mode
        if(BotConfig.getAdminIds().contains(event.getUser().getId()) && !BotConfig.getDevMode()) {
            bot.getTopggManager().updateStats(totalGuildCount).thenAccept(success -> {
              if(success) {
                    event.getHook().sendMessage(String.format("%s Server count successfully sent to top.gg.", BotConfig.yesEmoji())).setEphemeral(true).queue();
                } else {   
                    event.getHook().sendMessage(":warning: Failed to update stats on top.gg.").setEphemeral(true).queue();
              }  
            });
        }
    }

    private String getFormattedUptime(Localization localization) {
        // Calculate JVM uptime to get an idea of the bot uptime +- a few seconds
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMillis);

        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;

        // Format uptime to exclude zero units
        StringBuilder uptimeBuilder = new StringBuilder();
        if (days > 0) {
            uptimeBuilder.append(days).append(localization.getMsg("stats_command", "uptime_days"));
        }
        if (hours > 0) {
            uptimeBuilder.append(hours).append(localization.getMsg("stats_command", "uptime_hours"));
        }
        if (minutes > 0) {
            uptimeBuilder.append(minutes).append(localization.getMsg("stats_command", "uptime_minutes"));
        }
        
        if (seconds > 0 || uptimeBuilder.length() == 0) { // Ensure at least seconds is shown if all others are 0
            uptimeBuilder.append(seconds).append(" " + localization.getMsg("stats_command", "uptime_seconds"));
        } else {
            // Remove the trailing comma and space if seconds are the only unit
            uptimeBuilder.setLength(uptimeBuilder.length() - 2);
        }

        return uptimeBuilder.toString();
    }
}