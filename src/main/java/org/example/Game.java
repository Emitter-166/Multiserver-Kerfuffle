package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Game {
    List<String> players = new ArrayList<>();
    Map<String, Integer> points = new HashMap<>();
    Map<String, String> profilePics = new HashMap<>();
    Map<String, String> playerNames = new HashMap<>();
    String channelId;
    long startedOn = 0L;
    boolean gameStarted = false;
    long nextTaskOn = 0L;

    Game(String channelId){
        this.channelId = channelId;
    }

    void sendJoiningMessage(String title){
        if(channelId == null) return;

        Button starterButton = Button.success(channelId + "-starter", "⚔️ start");
        Button joiningButton = Button.primary(channelId + "-joining", "\uD83D\uDD25 Join");

        MessageCreateBuilder message = new MessageCreateBuilder();

        message.addEmbeds(new EmbedBuilder()
                .setTitle(title)
                .addField("Gathering Players", "" +
                        " \n" +
                        ":fire: **to join the fight** \n" +
                        ":crossed_swords: **to start the game**", false).build());
        message.setActionRow(starterButton, joiningButton);

        Main.jda.getTextChannelById(channelId).sendMessage(message.build()).queue();

    }

    String addPlayer(User user, String channelId){
       if(players.contains(user.getId())) return "already in";
       if(gameStarted) return "game started";

       players.add(user.getId());
       profilePics.put(user.getId(), user.getEffectiveAvatarUrl());
       playerNames.put(user.getId(), user.getName());
       points.put(user.getId(), 0);

       return "added";
    }

    void sendRemaining(){
        StringBuilder remaining = new StringBuilder();
        remaining.append("```");
        playerNames.forEach((id, playerName) -> remaining.append(playerName).append("\n"));
        remaining.append("```");
        Main.jda.getTextChannelById(channelId).sendMessageEmbeds(new EmbedBuilder()
                .setTitle(playerNames.size() + " Players remaining")
                .setDescription(remaining.toString())
                .build()).queue();

    }

    void start(){
        startedOn = System.currentTimeMillis();
        gameStarted = true;
        nextTaskOn  = System.currentTimeMillis() + 10_000;
    }
}
