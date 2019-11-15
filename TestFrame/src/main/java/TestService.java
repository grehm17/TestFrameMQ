import interfaces.Sender;

import java.lang.reflect.Constructor;

public class TestService {
    public static void main(String[] args){
        String testOperationType = args[0];
        Sender senderClass = null;
        try{
            Class<?> senderClassName = Class.forName(testOperationType+"Sender");
            Constructor<?> senderConstructor = senderClassName.getConstructor(String.class);
            senderClass = (Sender) senderConstructor.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
