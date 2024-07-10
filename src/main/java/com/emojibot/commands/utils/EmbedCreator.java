package com.emojibot.commands.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;


import com.emojibot.BotConfig;


public class EmbedCreator {

    public static MessageEmbed createError(String errorMessage) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(BotConfig.noEmoji() + " " + errorMessage)
                .build();
    }

    public static MessageEmbed createDefault(String message) {
        return new EmbedBuilder()
                .setColor(BotConfig.getGeneralEmbedColor())
                .setDescription(message)
                .build();
    }

    public static MessageEmbed createSuccess(String message) {
        return new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription(BotConfig.yesEmoji() + " " + message)
                .build();
    }

}

