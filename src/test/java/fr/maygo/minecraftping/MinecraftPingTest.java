package fr.maygo.minecraftping;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MinecraftPingTest {

    @Test
    public void testPing(){
        try {
            MinecraftPinger.pingServer("mc.hypixel.net");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
