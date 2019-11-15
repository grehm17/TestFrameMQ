package interfaces;

public interface Sender extends Endpoint{
    void loadMessage(String filename);
    boolean checkResult(String message);
}
