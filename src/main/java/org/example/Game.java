package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.Database.connection;
import static org.example.ImageProcessing.randomChance;

public class Game {
    List<String> players = new ArrayList<>();
    Map<String, Integer> points = new HashMap<>();
    Map<String, String> profilePics = new HashMap<>();
    Map<String, String> playerNames = new HashMap<>();
    String channelId;
    long startedOn = 0L;
    boolean gameStarted = false;
    String title;
    Game(String channelId){
        this.channelId = channelId;
    }
    Message starterMessage = null;

    void sendJoiningMessage(String title){
        if(channelId == null) return;
        this.title = title;
        Button starterButton = Button.success(channelId + "-starter", "⚔️ start");
        Button joiningButton = Button.primary(channelId + "-joining", "\uD83D\uDD25 Join");

        AtomicReference<MessageCreateBuilder> message = new AtomicReference<>(new MessageCreateBuilder());

        message.get().addEmbeds(new EmbedBuilder()
                .setTitle(title)
                .addField("Gathering Players", "" +
                        " \n" +
                        ":fire: **to join the fight** \n" +
                        ":crossed_swords: **to start the game**", false).build());
        message.get().setActionRow(starterButton, joiningButton);

        message.get().addEmbeds(new EmbedBuilder()
                        .setTitle("⚔️ " + players.size() + " Players Joined")
                        .build());
        Main.jda.getTextChannelById(channelId).sendMessage(message.get().build()).queue(msg ->{
            starterMessage = msg;
        });

    }

    String addPlayer(User user){
       if(players.contains(user.getId())) return "already in";
       if(gameStarted) return "game started";

       players.add(user.getId());
       profilePics.put(user.getId(), user.getEffectiveAvatarUrl());
       playerNames.put(user.getId(), user.getName());
       points.put(user.getId(), 0);
        if(starterMessage == null) return "";
       starterMessage.editMessageEmbeds(new EmbedBuilder()
               .setTitle(title)
               .addField("Gathering Players", "" +
                       " \n" +
                       ":fire: **to join the fight** \n" +
                       ":crossed_swords: **to start the game**", false).build(),new EmbedBuilder()
               .setTitle("⚔️ " + players.size() + " Players Joined")
               .build()).queue();
       return "added";
    }

    void sendRemaining(){
        StringBuilder remaining = new StringBuilder();
        remaining.append("``` \n");
        playerNames.forEach((id, playerName) -> remaining.append(playerName).append("\n"));
        remaining.append("```");
        Main.jda.getTextChannelById(channelId).sendMessageEmbeds(new EmbedBuilder()
                .setTitle(playerNames.size() + " Players remaining")
                .setDescription(remaining.toString())
                .build()).queue();

    }

    void start(){
        gameStarted = true;
        startedOn = System.currentTimeMillis();
    }

    void process(){
        Runnable run = () ->{
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            start();
            sendRemaining();
           while (players.size() != 0){
               try {
                   combineAndSend();
                   Thread.sleep(10_000);
               } catch (Exception e) {
                   throw new RuntimeException(e);
               }
           }
           Main.games.remove(channelId);
        };
        Thread thread = new Thread(run);
        thread.start();
    }
    void combineAndSend() throws SQLException, IOException {
        if(players.size() > 1){
            if(players.size() % 10 == 0 || players.size() == 4){
                sendRemaining();
            }
            if(randomChance(7) || players.size() == 3){
                //single
                String killedId = players.get((int)Math.floor(Math.random() * players.size()));
                String killedName = playerNames.get(killedId);
                String death_message = "**" + connection.createStatement().executeQuery("SELECT * FROM death_messages ORDER BY RANDOM() LIMIT 1")
                        .getString("message").replace("x ", killedName + " ").replace(" x ", " " + killedName + " ") + "**";
                ImageProcessing.combineOneImage(killedId, channelId , death_message);

                players.remove(killedId);
                playerNames.remove(killedId);
                profilePics.remove(killedId);
            }else{
                //dual
                String killerId = players.get((int)Math.floor(Math.random() * players.size()));
                String killer_name = playerNames.get(killerId);
                
                String killedId;
                do{
                    killedId = players.get((int) Math.floor(Math.random() * players.size()));
                }while(killerId.equalsIgnoreCase(killedId));
                String killed_name = playerNames.get(killedId);
                String kill_message = "**" + connection.createStatement().executeQuery("SELECT * FROM kill_messages ORDER BY RANDOM() LIMIT 1")
                        .getString("message").replace("x ", killer_name + " ").replace(" x ", " " + killer_name + " ")
                        .replace(" y ", " " + killed_name + " ") + "**";
                if(kill_message == null) kill_message = killer_name + " killed " + killed_name;
                ImageProcessing.combineTwoImages(killerId, killedId, channelId, kill_message);
                points.put(killerId, points.get(killerId) + 1);
                players.remove(killedId);
                playerNames.remove(killedId);
                profilePics.remove(killedId);
                
            }
        }else if(players.size() == 1){
            String winnerId = players.get(0);
            Statement statement = connection.createStatement();
            String serverId = Main.jda.getTextChannelById(channelId).getGuild().getId();

            try {
                int previousGames = connection.createStatement().executeQuery("SELECT games FROM servers WHERE _id == '" + serverId + "'")
                        .getInt("games");
                connection.createStatement().execute(String.format("UPDATE servers SET games = %s WHERE _id == '%s'", previousGames + 1, serverId));
            } catch (SQLException exception) {
                connection.createStatement().execute(String.format("INSERT INTO servers VALUES('%s', 1)", serverId));
            }

            int previousWins;
            try {
                previousWins = statement.executeQuery("SELECT wins FROM winners WHERE _id == '" + winnerId + "'").getInt("wins");
                statement.execute(String.format("UPDATE winners SET wins = %s WHERE _id == '%s'", previousWins + 1, winnerId));
            } catch (SQLException exception) {
                statement.execute(String.format("INSERT INTO winners VALUES('%s', 1)", winnerId));
                previousWins = 1;
            }
            //winner
            ImageProcessing.combineOneImage(winnerId, channelId, points.get(winnerId), startedOn, previousWins);
            players.remove(winnerId);
            Main.games.remove(channelId, this);
        }
    }
}
