package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.Localization;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class PingCommand extends EmojiCommand {

    public PingCommand(Bot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Pong! - Shows the latency of the bot";
        this.cooldownDuration = 6;

        this.localizedNames.put(DiscordLocale.TURKISH, "ping");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Pong! - Botun gecikmesini gösterir");
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        event.reply(String.format(localization.getMsg("ping_command", "pong"), gatewayPing, restPing)).queue();

    }
}
