package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.example.Database.connection;

public class Main {
    public static JDA jda;
    public static Map<String, Game> games = new HashMap<>();

    public static void main(String[] args) throws LoginException, InterruptedException {
        jda = JDABuilder.createLight(Tokens.TOKEN)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Commands())
                .build().awaitReady();
        while (true){
            games.forEach((channelId, game) ->{
                try {
                    gameProcess(game);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    static void gameProcess(Game game) throws SQLException, IOException {
        if(game.gameStarted){
            if(game.nextTaskOn > System.currentTimeMillis()) return;
            if(game.players.size() != 1){
                String killerId = game.players.get((int) Math.round(Math.random() * game.players.size()));
                String killedId;
                String killer_name = Main.jda.getUserById(killerId).getName();
                boolean isSingleDeath;
                if(game.players.size() == 3){
                    isSingleDeath = true;
                }else{
                    isSingleDeath = Math.round(Math.random() * 6) == 1;
                }

                if(isSingleDeath){
                   String death_message = "**" + connection.createStatement().executeQuery("SELECT * FROM death_messages ORDER BY RANDOM() LIMIT 1")
                            .getString("message").replace("x ", killer_name + " ").replace(" x ", " " + killer_name + " ") + "**";
                    ImageProcessing.combineOneImage(killerId, game.channelId ,death_message);
                    game.players.remove(killerId);
                    game.playerNames.remove(killerId);
                    game.profilePics.remove(killerId);

                }else{
                    do{
                        killedId = game.players.get((int) Math.round(Math.random() * game.players.size()));
                    }while(killerId.equalsIgnoreCase(killedId));

                    String killed_name = Main.jda.getUserById(killedId).getName();
                    String kill_message = "**" + connection.createStatement().executeQuery("SELECT * FROM kill_messages ORDER BY RANDOM() LIMIT 1")
                            .getString("message").replace("x ", killer_name + " ").replace(" x ", " " + killer_name + " ")
                            .replace(" y ", " " + killed_name + " ") + "**";
                   ImageProcessing.combineTwoImages(killerId, killedId, game.channelId, kill_message);

                   game.points.put(killerId, game.points.get(killerId) + 1);
                   game.players.remove(killedId);
                   game.playerNames.remove(killedId);
                   game.profilePics.remove(killedId);
                }
                if(game.players.size() % 10 == 0 || game.players.size() == 4){
                    game.sendRemaining();
                }
                game.nextTaskOn = System.currentTimeMillis() + 10_000;
            }else{
                String winnerId = game.players.get(0);
                Statement statement = connection.createStatement();
                int previousGames;

                String serverId = jda.getTextChannelById(game.channelId).getGuild().getId();
                try {
                    previousGames = connection.createStatement().executeQuery("SELECT games FROM servers WHERE _id == '" + serverId + "'")
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
                ImageProcessing.combineOneImage(winnerId, game.channelId, game.points.get(winnerId), game.startedOn, previousWins);

                games.remove(game.channelId);
            }
        }
    }
}