package cirrus;

import java.util.Random;

public class Flagger
{
    private Random rand;

    public Flagger()
    {
        rand = new Random();
    }

    public boolean flag(String filename)
    {
        return rand.nextDouble() < 0.5;
    }
}
