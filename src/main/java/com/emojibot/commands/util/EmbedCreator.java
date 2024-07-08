package com.emojibot.commands.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;

import java.awt.Color;

import org.jetbrains.annotations.NotNull;

import com.emojibot.BotConfig;

/**
 * Utility class storing helpful methods for embeds.
 *
 * @author Technovision
 */
public class EmbedCreator {

    /** Custom Emojis. */
    public static final String GREEN_TICK = "<:green_tick:800555917472825418>";
    public static final String BLUE_TICK = "<:blue_tick:800623774293819413>";
    public static final String RED_X = "<:red_x:800554807164665916>";
    public static final String BLUE_X = "<:blue_x:800623785736798228>";


    public static @NotNull MessageEmbed createError(String errorMessage) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(BotConfig.noEmoji() + " " + errorMessage)
                .build();
    }

    public static @NotNull MessageEmbed createDefault(String message) {
        return new EmbedBuilder()
                .setColor(BotConfig.getGeneralEmbedColor())
                .setDescription(message)
                .build();
    }

    public static @NotNull MessageEmbed createSuccess(String message) {
        return new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription(BotConfig.yesEmoji() + " " + message)
                .build();
    }

}

