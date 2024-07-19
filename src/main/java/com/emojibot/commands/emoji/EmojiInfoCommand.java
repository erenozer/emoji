package com.emojibot.commands.emoji;

import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import com.emojibot.Bot;
import com.emojibot.BotConfig;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class EmojiInfoCommand extends Command {
    public EmojiInfoCommand(Bot bot) {
        super(bot);
        this.name = "info";
        this.description = "Get information about an emoji";
        this.cooldownDuration = 3;

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "emoji", "Emoji/name from this server to get it's info", true, false);
        this.args.add(emojiNameArgument);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        // Using embeds, defer the reply to use the hook later
        event.deferReply().queue();

        String emojiInput = EmojiInput.normalize(Objects.requireNonNull(event.getOption("emoji")).getAsString());
        String emojiName = EmojiInput.extractEmojiName(emojiInput);

        RichCustomEmoji emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        if (emoji == null) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " Make sure the emoji you are looking for is from **this server**.").setEphemeral(true).queue();
        } else {
            StringBuilder response = new StringBuilder();
            response.append("Information for the emoji `").append(emojiName).append("`:\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedDate = emoji.getTimeCreated().format(formatter);

            MessageEmbed infoEmbed = new EmbedBuilder()
                    .setTitle("Emoji Information")
                    .setColor(BotConfig.getGeneralEmbedColor())
                    //.setDescription("Information for the emoji `" + emojiName + "`:")
                    .addField("ID", emoji.getId(), true)
                    .addField("Name", emoji.getName(), true)
                    .addField("Date Added", formattedDate, true)
                    .addField("Animated?", emoji.isAnimated() ? "Yes" : "No", true)
                    .addField("Managed?", emoji.isManaged() ? "Yes" : "No", true)
                    .addField("Usage", "`<"+emoji.getName()+":"+emoji.getId()+">`", true)
                    .setThumbnail(emoji.getImageUrl())
                    .build();

            event.getHook().sendMessageEmbeds(infoEmbed).queue();

        }
    }

}