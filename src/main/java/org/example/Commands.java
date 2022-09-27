package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import static org.example.Database.connection;

public class Commands extends ListenerAdapter {
    public static final String BOT_NAME = "Fun games";
    public void onMessageReceived(MessageReceivedEvent e){
        String msg = e.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
        if(!msg.startsWith("--")) return;
        switch (msg){
            case "--help":
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(String.format("Help commands for %s", BOT_NAME))
                        .setDescription("`--ke or --kerfuffle` **Start a new kerfuffle game!**");
                e.getMessage().replyEmbeds(builder.build()).mentionRepliedUser(false).queue();
                break;
            case "--ke": case "--kerfuffle":
                if(Main.games.containsKey(e.getChannel().getId())){
                    e.getMessage().replyEmbeds(new EmbedBuilder()
                            .setTitle("We don't want too much kerfuffle \uD83D\uDE02")
                            .build()).mentionRepliedUser(false).queue();
                }else{
                    Game game = new Game(e.getChannel().getId());
                    Main.games.put(e.getChannel().getId(), game);
                    int previousGames;
                    String serverId = e.getGuild().getId();
                    try {
                        previousGames = connection.createStatement().executeQuery("SELECT games FROM servers WHERE _id == '" + serverId + "'")
                                .getInt("games") +1;
                    } catch (SQLException exception) {
                        try {
                            connection.createStatement().execute(String.format("INSERT INTO servers VALUES('%s', 1)", serverId));
                            previousGames = 1;
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    String title = e.getGuild().getName() + "'s " + numString(previousGames) + " Kerfuffle Game";
                    game.sendJoiningMessage(title);
                }
        }
    }

    public void onButtonInteraction(ButtonInteractionEvent e){
        String[] id = e.getButton().getId().split("-");
        if(id[1].equalsIgnoreCase("joining")){
            if(Main.games.containsKey(id[0])){
              String response = Main.games.get(id[0]).addPlayer(e.getUser());
              if(response.equalsIgnoreCase("added")){
                  e.getInteraction().replyEmbeds(new EmbedBuilder()
                          .setTitle("you are in! ⚔️")
                          .build()).setEphemeral(true).queue();
              }else if(response.equalsIgnoreCase("already in")){
                  e.getInteraction().replyEmbeds(new EmbedBuilder()
                          .setTitle("You are already in! ✅")
                          .build()).setEphemeral(true).queue();
              }else{
                  e.getInteraction().replyEmbeds(new EmbedBuilder()
                          .setTitle("Game started, you can't join! ⛔")
                          .build()).setEphemeral(true).queue();
              }

            }else{
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                                .setTitle("That game is not running! ⚔️")
                                .build()).setEphemeral(true).queue();
            }
        }else if(id[1].equalsIgnoreCase("starter")){
            if(!e.getMember().hasPermission(Permission.MODERATE_MEMBERS)){
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                        .setTitle("You aren't allowed to do that! ⛔")
                        .build()).setEphemeral(true).queue();
                return;
            }
            if(Main.games.get(e.getChannel().getId()).gameStarted){
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                        .setTitle("The game has already started! ⛔")
                        .build()).setEphemeral(true).queue();
                return;
            }
            if(Main.games.get(e.getChannel().getId()).players.size() >= 2){
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                        .setTitle("At least 3 players required ⛔")
                        .build()).setEphemeral(true).queue();
                return;
            }
            if(Main.games.containsKey(id[0])){
                Main.games.get(id[0]).process();
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                        .setTitle("Starting the game!")
                        .setColor(Color.green)
                        .build()).setEphemeral(true).queue();
            }else{
                e.getInteraction().replyEmbeds(new EmbedBuilder()
                        .setTitle("That game is not running! ⛔")
                        .setColor(Color.green)
                        .build()).setEphemeral(true).queue();
            }
        }

    }
    static String numString(int number){
        char first = (number + "").toCharArray()[(number + "").toCharArray().length -1];
        return switch (first) {
            case '1' -> number + "st";
            case '2' -> number + "nd";
            case '3' -> number + "rd";
            default -> number + "th";
        };
    }
}
