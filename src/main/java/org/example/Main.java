package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import java.util.concurrent.ConcurrentHashMap;
public class Main {
    public static JDA jda;
    public static ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception{
        jda = JDABuilder.createLight(Tokens.TOKEN)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new Commands())
                .build().awaitReady();
        jda.getPresence().setPresence(Activity.listening("--help"), true);
    }
}