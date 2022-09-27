package org.example;

import java.sql.SQLException;

public class Setting {
    public static void main(String[] args) throws SQLException {
        String death = "x ate too much Taco bell\n" +
                "x Left the chat due to too much kindness.";

        String kill = " y was given the cold shoulder by x \n" +
                " y was struck with Cupid's arrow by x \n" +
                " y 's heart was broken by x \n" +
                " y was out of the league of x \n" +
                " y was hit off by a love bomb from x \n" +
                " y was sacrificed by x \n" +
                " y was neutralized by x \n" +
                " y tried to escape their fate determined by x \n" +
                " y was offered to the Void God by x \n" +
                " y was thrown a frisbee by x \n" +
                " y got WOOF'D by x into the void \n" +
                " y was distracted by a puppy planted by x \n" +
                " y was mushed by x \n" +
                " y got banana pistol'd by x \n" +
                " y was tranquilized by x \n" +
                " y was extinguished by x \n" +
                " y got rekt by x \n" +
                " y got bamboozled by x \n" +
                " y got dabbed on by x \n" +
                " x took the L to y \n" +
                " y was deleted by x \n" +
                " y was crashed by x \n" +
                " y was ALT-F4'd by x \n" +
                " y received a shock from x \n" +
                " y was turned into a space dust by x \n" +
                " y was hit by an asteroid from x \n" +
                " y was retrograded by x \n" +
                " y was sent into orbit by x \n" +
                " y was launched onto the sun by x \n" +
                " y blushed so hard they became x 's garden tomato\n" +
                " y was turned into a donut and sold to their friend by x . Poor friend didn't know it was y .";

        for(String msg : death.split("\n")){
            try{
                Database.connection.createStatement().execute(String.format("INSERT INTO death_messages(message) VALUES (\"%s\")", msg));
            }catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        }
    }
}
