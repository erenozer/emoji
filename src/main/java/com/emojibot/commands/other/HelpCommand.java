package com.emojibot.commands.other;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand extends Command {

    public HelpCommand(Bot bot) {
        super(bot);
        this.name = "start";
        this.description = "Learn about the commands available to you!";

    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        MessageEmbed helpEmbed = new EmbedBuilder()
        .setAuthor("Hi there! I'm Emoji.", "https://discord.com/oauth2/authorize?client_id=414878659267133445", "https://cdn.discordapp.com/emojis/728286263429300274.webp?size=240&quality=lossless")
        .setDescription("[Add me to your server!](https://discord.com/oauth2/authorize?client_id=414878659267133445)\nYou can find new emojis, search for emojis, and more!\nStart typing **/** (slash) to use my commands.\n\n[Need some help? Click here.](https://discord.gg/U5v2csS)")
        .addField("Featured Command", ":star: Search: Find a specific emoji that you like using our extensive search system!", false)
        .addField("All Commands", "**search**, **link**, **info**, **emojify**, **start**, **ping**\n\n>>> Example command usage: `/info`", false)
        .setColor(BotConfig.getGeneralEmbedColor())
        .setFooter("Made with ♥ by eren.im and ardasoyturk", event.getJDA().getSelfUser().getAvatarUrl())
        .setImage("https://cdn.discordapp.com/attachments/730414657478721607/733409659612037232/emoji.png")
        .build();

        event.getHook().sendMessageEmbeds(helpEmbed).queue();

    }
}
