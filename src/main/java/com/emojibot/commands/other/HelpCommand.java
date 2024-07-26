package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.EmojiCommand;
import com.emojibot.commands.utils.language.Localization;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;


public class HelpCommand extends EmojiCommand {

    public HelpCommand(Bot bot) {
        super(bot);
        this.name = "help";
        this.description = "Learn about what the commmands do";
        this.cooldownDuration = 3;
    
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());
        
        

        MessageEmbed helpEmbed = new EmbedBuilder()
        .setAuthor(localization.getMsg("help_command", "title"), "https://discord.com/oauth2/authorize?client_id=414878659267133445")
        //.setDescription("[Add me to your server!](https://discord.com/oauth2/authorize?client_id=414878659267133445)\nYou can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\n[Need some help? Click here.](https://discord.gg/U5v2csS)")
        .setDescription(localization.getMsg("help_command", "desc"))
        .addField(localization.getMsg("help_command", "all_commands"), localization.getMsg("help_command", "all_commands_desc"), false)
        .setColor(BotConfig.getGeneralEmbedColor())
        .setFooter(localization.getMsg("help_command", "made_with"), event.getJDA().getSelfUser().getAvatarUrl())
        //.setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
        .build();
        
        Button inviteButton = Button.of(ButtonStyle.LINK, "https://discord.com/oauth2/authorize?client_id=414878659267133445", localization.getMsg("help_command", "add_me_to_server"), Emoji.fromFormatted("<:emoji:728286263429300274>"));
        Button serverButton = Button.of(ButtonStyle.LINK, "https://discord.gg/U5v2csS", localization.getMsg("help_command", "join_support_server"), Emoji.fromUnicode("❓"));
        event.getHook().sendMessageEmbeds(helpEmbed).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();

    }
}
