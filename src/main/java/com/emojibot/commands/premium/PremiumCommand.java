package com.emojibot.commands.premium;

import java.awt.Color;
import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.PremiumManager;
import com.emojibot.utils.command.EmojiCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class PremiumCommand extends EmojiCommand {

    public PremiumCommand(Bot bot) {
        super(bot);
        this.name = "premium";
        this.description = "Learn about the premium features of the Emoji!";
        this.cooldownDuration = 3;

        this.localizedNames.put(DiscordLocale.TURKISH, "premium");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Emoji Premium özellikleri hakkında bilgi alın!");
    
        OptionData option = new OptionData(OptionType.STRING, "activate", "Fill this argument to get information regarding activating the premium.", false);
        option.setNameLocalization(DiscordLocale.TURKISH, "aktifleştir");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Bu argümanı doldurarak Emoji Premium'un nasıl aktif hale getirileceği hakkında bilgi alabilirsin.");

        this.args.add(option);

    }


    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // If the user is an admin and the command is run on Emoji Bot/Testing server and an argument (server ID) is provided
        // Use controls to (de)activate premium for given server
        if(event.getOptions().size() > 0 && BotConfig.getAdminIds().contains(event.getUser().getId()) && (event.getGuild().getId().equals("232918641866178560") || event.getGuild().getId().equals("368839796703100928") )) {
            // Admin control for premium servers
            var serverIdInput = event.getOption("activate").getAsString();

            PremiumManager.askForConfirmation(event.getHook(), serverIdInput);

            return;
        }

        Localization localization = Localization.getLocalization(event.getUser().getId());

        // Check if the server has premium enabled, if so, inform the user and send the premium information
        var serverPremiumStatus = PremiumManager.getPremiumStatus(event.getGuild().getId());
        
        if(event.getOptions().size() == 0) {
            MessageEmbed premiumInfo = new EmbedBuilder()
            .setAuthor(localization.getMsg("premium_command", "title"), "https://buymeacoffee.com/erenozer")
            //.setDescription("[Add me to your server!](https://discord.com/oauth2/authorize?client_id=414878659267133445)\nYou can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\n[Need some help? Click here.](https://discord.gg/U5v2csS)")
            .setDescription(localization.getMsg("premium_command", "desc"))
            .addField(localization.getMsg("premium_command", "benefits"), localization.getMsg("premium_command", "benefits_desc"), false)
            .addField(localization.getMsg("premium_command", "how_to_get"), localization.getMsg("premium_command", "how_to_get_desc"), false)
            .setColor(Color.YELLOW)
            .setFooter(localization.getMsg("premium_command", "footer"), event.getJDA().getSelfUser().getAvatarUrl())
            .setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
            .build();
            
            Button inviteButton = Button.of(ButtonStyle.LINK, "https://buymeacoffee.com/erenozer", localization.getMsg("premium_command", "buy_premium"), Emoji.fromFormatted("<:emoji:728286263429300274>"));
            Button serverButton = Button.of(ButtonStyle.LINK, "https://discord.gg/U5v2csS", localization.getMsg("premium_command", "join_support_server"), Emoji.fromUnicode("⭐"));
            event.getHook().sendMessageEmbeds(premiumInfo).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();

  
        } else {
            // Send the activation information about premium
            MessageEmbed premiumInfo = new EmbedBuilder()
            .addField(localization.getMsg("premium_command", "how_to_activate"), localization.getMsg("premium_command", "how_to_activate_desc"), false)
            .setColor(Color.YELLOW)
            .setFooter(localization.getMsg("premium_command", "footer"), event.getJDA().getSelfUser().getAvatarUrl())
            .setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
            .build();
            
            Button inviteButton = Button.of(ButtonStyle.LINK, "https://buymeacoffee.com/erenozer", localization.getMsg("premium_command", "buy_premium"), Emoji.fromFormatted("<:emoji:728286263429300274>"));
            Button serverButton = Button.of(ButtonStyle.LINK, "https://discord.gg/U5v2csS", localization.getMsg("premium_command", "join_support_server"), Emoji.fromUnicode("⭐"));
            event.getHook().sendMessageEmbeds(premiumInfo).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();
        }

        // Inform that this guild has premium enabled
        if(serverPremiumStatus) {
        event.getHook().sendMessage(localization.getMsg("premium_command", "premium_enabled")).queue();
        }

    }
}
