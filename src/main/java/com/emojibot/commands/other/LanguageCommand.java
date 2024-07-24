package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.LanguageManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LanguageCommand extends Command {

    public LanguageCommand(Bot bot) {
        super(bot);
        this.name = "language";
        this.description = "Change the language of the bot - Botun dilini değiştirir!";
        this.cooldownDuration = 5;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        // Ask the user for language, this will send a message with buttons, and handle the response
        LanguageManager.askUserForLanguage(event.getHook());
        
    }
}
