package com.emojibot.commands.premium;

import com.emojibot.Bot;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.HideManager;
import com.emojibot.utils.button_listeners.PremiumManager;
import com.emojibot.utils.command.EmojiCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class HideCommand extends EmojiCommand {

    public HideCommand(Bot bot) {
        super(bot);
        this.name = "hide";
        this.description = "Hides your server's emojis from other servers";
        this.cooldownDuration = 5;

        this.localizedNames.put(DiscordLocale.TURKISH, "gizle");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucudaki emojileri diğer sunuculardan gizler");

        this.permission = Permission.MANAGE_SERVER;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String guildId = event.getGuild().getId();

        // Check if the server is premium
        if(PremiumManager.getPremiumStatus(guildId) != true) {
            event.getHook().sendMessage(localization.getMsg("premium_command", "need_premium")).queue();
            return;
        }
        
        // Use the HideManager to ask for the hide status with buttons to change it
        HideManager.askForHideStatus(event.getHook(), guildId);


    }
}
