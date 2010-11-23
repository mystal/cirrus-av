package cirrus.server;

public interface AntiVirus
{
    public abstract boolean scan(String filename);
}
