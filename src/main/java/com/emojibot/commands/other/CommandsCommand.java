package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;


public class CommandsCommand extends EmojiCommand {

    public CommandsCommand(Bot bot) {
        super(bot);
        this.name = "commands";
        this.description = "Learn about what the commmands do";
        this.cooldownDuration = 3;

        this.localizedNames.put(DiscordLocale.TURKISH, "komutlar");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Komutların açıklamalarını gösterir");

        this.botPermission = Permission.MESSAGE_EMBED_LINKS;
    
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());
        
        MessageEmbed helpEmbed = new EmbedBuilder()
        .setAuthor(localization.getMsg("commands_command", "title"), "https://discord.com/oauth2/authorize?client_id=414878659267133445")
        //.setDescription("[Add me to your server!](https://discord.com/oauth2/authorize?client_id=414878659267133445)\nYou can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\n[Need some help? Click here.](https://discord.gg/U5v2csS)")
        .setDescription(localization.getMsg("commands_command", "desc"))
        .addField(localization.getMsg("commands_command", "all_commands"), localization.getMsg("commands_command", "all_commands_desc"), false)
        .setColor(BotConfig.getGeneralEmbedColor())
        .setFooter(localization.getMsg("commands_command", "made_with"), event.getJDA().getSelfUser().getAvatarUrl())
        //.setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
        .build();
        
        Button inviteButton = Button.of(ButtonStyle.LINK, "https://discord.com/oauth2/authorize?client_id=414878659267133445", localization.getMsg("commands_command", "add_me_to_server"), Emoji.fromFormatted("<:emoji:728286263429300274>"));
        Button serverButton = Button.of(ButtonStyle.LINK, "https://discord.gg/U5v2csS", localization.getMsg("commands_command", "join_support_server"), Emoji.fromUnicode("❓"));
        event.getHook().sendMessageEmbeds(helpEmbed).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();

    }
}
