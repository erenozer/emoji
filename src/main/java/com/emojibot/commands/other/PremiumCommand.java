package com.emojibot.commands.other;

import java.awt.Color;

import com.emojibot.Bot;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.Localization;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Emoji'nin premium özellikleri hakkında bilgi alın!");
    
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        MessageEmbed helpEmbed = new EmbedBuilder()
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
        event.getHook().sendMessageEmbeds(helpEmbed).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();

    }
}
