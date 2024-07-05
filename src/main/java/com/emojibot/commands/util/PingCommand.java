package com.emojibot.commands.util;

import com.emojibot.Bot;
import com.emojibot.commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends Command {

    public PingCommand(Bot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Pong!";

    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();

    }
}
