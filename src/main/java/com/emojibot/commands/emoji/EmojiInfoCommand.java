package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.EmojiInput;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.Localization;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class EmojiInfoCommand extends EmojiCommand {
    public EmojiInfoCommand(Bot bot) {
        super(bot);
        this.name = "info";
        this.description = "Get information about an emoji";

        this.localizedNames.put(DiscordLocale.TURKISH, "bilgi");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "İstediğiniz bir emoji hakkında bilgi verir");

        this.cooldownDuration = 4;

        OptionData option = new OptionData(OptionType.STRING, "emoji", "Emoji/name from this server to get it's info", true, false);
        option.setNameLocalization(DiscordLocale.TURKISH, "emoji");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Bilgisini almak istediğiniz emoji veya ismi");

        this.args.add(option);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        // Using embeds, defer the reply to use the hook later
        event.deferReply().queue();

        // Get the localization manager for the user
        Localization localization = Localization.getLocalization(event.getUser().getId());

        String emojiNotFoundMsg = localization.getMsg("info_command", "emoji_not_found");

        String emojiInput = EmojiInput.normalize(Objects.requireNonNull(event.getOption("emoji")).getAsString());
        String emojiName = EmojiInput.extractEmojiName(emojiInput);

        RichCustomEmoji emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        if (emoji == null) {
            event.getHook().sendMessage(String.format(emojiNotFoundMsg, BotConfig.noEmoji())).setEphemeral(true).queue();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
            String formattedDate = emoji.getTimeCreated().format(formatter);

            MessageEmbed infoEmbed = new EmbedBuilder()
                    .setTitle(localization.getMsg("info_command", "emoji_info"))
                    .setColor(BotConfig.getGeneralEmbedColor())
                    //.setDescription("Information for the emoji `" + emojiName + "`:")
                    .addField("ID", emoji.getId(), true)
                    .addField(localization.getMsg("info_command", "name"), emoji.getName(), true)
                    .addField(localization.getMsg("info_command", "date"), formattedDate, true)
                    .addField(localization.getMsg("info_command", "animated"), emoji.isAnimated() ? BotConfig.yesEmoji() : BotConfig.noEmoji(), true)
                    .addField(localization.getMsg("info_command", "managed"), emoji.isManaged() ? BotConfig.yesEmoji() : BotConfig.noEmoji(), true)
                    .addField(localization.getMsg("info_command", "usage"), "`<"+emoji.getName()+":"+emoji.getId()+">`", true)
                    .setThumbnail(emoji.getImageUrl())
                    .build();

            event.getHook().sendMessageEmbeds(infoEmbed).queue();

        }
    }

}