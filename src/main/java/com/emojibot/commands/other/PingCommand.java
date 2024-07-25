package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends Command {

    public PingCommand(Bot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Pong!";
        this.cooldownDuration = 6;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        event.reply(String.format("<:emoji:728286263429300274> Pong! | Gateway Ping: %sms, Rest Ping: %sms", gatewayPing, restPing)).queue();

    }
}
