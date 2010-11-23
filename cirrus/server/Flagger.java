package cirrus.server;

import cirrus.server.AntiVirus;

import java.util.Random;

public class Flagger implements AntiVirus
{
    private Random rand;

    public Flagger()
    {
        rand = new Random();
    }

    public boolean scan(String filename)
    {
        return rand.nextDouble() < 0.5;
    }
}
