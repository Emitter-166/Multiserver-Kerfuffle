package org.example;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
public abstract class ImageProcessing {
    static String path = "assets/";
    public static void combineTwoImages(String user1, String user2, String channelId, String killMessage) throws IOException {
        BufferedImage pfp1 = ImageIO.read(new URL(Main.games.get(channelId).profilePics.get(user1)));
        BufferedImage pfp2 = ImageIO.read(new URL(Main.games.get(channelId).profilePics.get(user2)));

        BufferedImage frame = ImageIO.read(new File(path + "dual.png"));
        BufferedImage overlay = ImageIO.read(new File(path + "frame2.png"));
        BufferedImage vsOverlay = ImageIO.read(new File(path + "vs.png"));

        BufferedImage finalImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        String generated = user1 + "vs" + user2 + ".png";

        Graphics2D g = finalImage.createGraphics();
        g.drawImage(frame, 0, 0, null);
        g.drawImage(pfp1, 33, 124, 199, 193, null);
        g.drawImage(pfp2, 266, 124, 200, 192, null);
        g.drawImage(overlay, 0, 0, null);
        g.drawImage(vsOverlay, 0, 0, null);
        g.dispose();
        ImageIO.write(finalImage, "PNG", new File(generated));

        send(generated, killMessage, channelId);
    }

    public static void combineOneImage(String userId, String channelId, String killMessage) throws IOException {
        combineOneImage(userId, channelId, false, killMessage, 0, 0, 0);
    }

    public static void combineOneImage(String userId, String channelId, int kills, long started, int wins) throws IOException {
        combineOneImage(userId, channelId, true, "", kills, started, wins);
    }
    private static void combineOneImage(String userId, String channelId, boolean isWinner, String killMessage, int kills, long started, int wins) throws IOException {
        BufferedImage frame = isWinner ? ImageIO.read(new File(path +"winner.png")) : ImageIO.read(new File(path +"single.png"));
        BufferedImage overlay_frame = ImageIO.read(new File(path + "frame1.png"));
        BufferedImage pfp = ImageIO.read(new URL(Main.games.get(channelId).profilePics.get(userId)));
        BufferedImage finalImage = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = finalImage.createGraphics();
        g.drawImage(frame, 0 , 0, null);
        g.drawImage(pfp, 135, 114, 229, 216, null);
        g.drawImage(overlay_frame, 0, 0, null);
        g.dispose();

        ImageIO.write(finalImage, "PNG", new File(userId+".png"));
        String generated = userId+".png";
        if(isWinner){
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Winner!")
                    .setDescription(String.format(":tada: <@%s> won this game! \n" +
                            ":skull: **Total kills:** `%s` \n" +
                            ":stopwatch: **Time survived:** `%s` seconds\n" +
                            ":trophy: **Total win in server:** `%s`", userId, kills , (System.currentTimeMillis() - started) / 1000, wins));

            File file = new File(generated);
            Main.jda.getTextChannelById(channelId).sendMessageEmbeds(builder.build())
                    .addFiles(FileUpload.fromData(file))
                    .queue(msg -> file.delete());

            return;
        }
        send(generated, killMessage, channelId);
    }

    public static boolean randomChance(int one_in){
        if(one_in == 1) return true;
        if(one_in == 0) return false;
        Random random = new Random();
       if( random.nextInt(one_in) == 1) return true;
       return false;
    }

    private static void send(String fileName, String killMessage, String channelId){
        File file = new File(fileName);
        Main.jda.getTextChannelById(channelId).sendMessageEmbeds(new EmbedBuilder()
                        .setDescription(killMessage).build())
                .addFiles(FileUpload.fromData(file)).queue(msg -> file.delete());
    }
}
