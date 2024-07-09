package com.emojibot.commands.emoji;

import com.emojibot.EmojiCache;
import com.emojibot.commands.Command;
import com.emojibot.Bot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.List;
import java.util.Objects;


// TODO: embed manager with nice emojis, implement the emoji info command with it


public class EmojiInfoCommand extends Command {
    private final EmojiCache emojiCache;

    public EmojiInfoCommand(Bot bot) {
        super(bot);
        this.name = "info";
        this.description = "Get information about an emoji";

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "emoji", "Emoji to get it's info", true, false);
        this.args.add(emojiNameArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
    String emojiName = normalize(Objects.requireNonNull(event.getOption("emoji")).getAsString());

    List<RichCustomEmoji> emojiList = emojiCache.getEmojis(emojiName);

        if (emojiList.isEmpty()) {
            event.reply("No custom emoji found with that name.").queue();
        } else {
            StringBuilder response = new StringBuilder();
            response.append("Information for the emoji `").append(emojiName).append("`:\n");

            for (RichCustomEmoji emoji : emojiList) {
                response.append("Name: ").append(emoji.getName()).append("\n");
                response.append("ID: ").append(emoji.getId()).append("\n");
                response.append("Creation date: ").append(emoji.getTimeCreated().toLocalDate()).append("\n");
                response.append("Is Animated?: ").append(emoji.isAnimated()).append("\n");
                response.append("Is managed?: ").append(emoji.isManaged()).append("\n");
                response.append("URL: ").append(emoji.getImageUrl()).append("\n\n");
            }

            event.reply(response.toString()).queue();
        }
    }


    private String normalize(String input) {
        return input.trim().toLowerCase().replaceAll("\\s+", "");
    }

}