package interfaces;

public interface Endpoint {
    void setInputSchema(String filename);
    String recieveMessage(); //возможно отказаться от интерфейсов и сделать абстрактный класс - обдумать.
    void sendMessage();
}
