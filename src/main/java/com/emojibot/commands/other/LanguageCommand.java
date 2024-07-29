package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.LanguageManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class LanguageCommand extends EmojiCommand {

    public LanguageCommand(Bot bot) {
        super(bot);
        this.name = "language";
        this.description = "Change the language of Emoji bot manually";
        this.cooldownDuration = 5;

        this.localizedNames.put(DiscordLocale.TURKISH, "dil");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Botun dilini değiştirmenizi sağlar");
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        // Ask the user for language, this will send a message with buttons, and handle the response
        LanguageManager.askUserForLanguage(event.getHook());

    }
}
