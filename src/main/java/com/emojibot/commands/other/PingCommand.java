package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends Command {

    public PingCommand(Bot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Pong!";

    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();

        event.reply(String.format("Pong! | Gateway Ping: %sms, Rest Ping: %sms", gatewayPing, restPing)).queue();

    }
}
