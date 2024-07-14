package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;


public class HelpCommand extends Command {

    public HelpCommand(Bot bot) {
        super(bot);
        this.name = "start";
        this.description = "Learn about the commands available to you!";
        this.cooldownDuration = 3;
    
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        MessageEmbed helpEmbed = new EmbedBuilder()
        .setAuthor("Hi there! I'm Emoji.", "https://discord.com/oauth2/authorize?client_id=414878659267133445")
        //.setDescription("[Add me to your server!](https://discord.com/oauth2/authorize?client_id=414878659267133445)\nYou can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\n[Need some help? Click here.](https://discord.gg/U5v2csS)")
        .setDescription("You can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\nJoin my [support server](https://discord.gg/U5v2csS) if you need assistance. :^)")
        .addField("Featured Command", ":star: Search: Find a specific emoji that you like using our extensive search system!", false)
        .addField("All Commands", "**list**, **upload**, **delete**, **search**, **link**, **info**, **emojify**, **start**, **ping**\n\n>>> Example command usage: `/info`", false)
        .setColor(BotConfig.getGeneralEmbedColor())
        .setFooter("Made with ♥ by eren.im and ardasoyturk", event.getJDA().getSelfUser().getAvatarUrl())
        .setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
        .build();

        Button inviteButton = Button.of(ButtonStyle.LINK, "https://discord.com/oauth2/authorize?client_id=414878659267133445", "Add me to your server", Emoji.fromFormatted("<:emoji:728286263429300274>"));
        Button serverButton = Button.of(ButtonStyle.LINK, "https://discord.gg/U5v2csS", "Join my support server", Emoji.fromUnicode("❓"));
        event.getHook().sendMessageEmbeds(helpEmbed).setComponents(ActionRow.of(inviteButton), ActionRow.of(serverButton)).queue();

    }
}
