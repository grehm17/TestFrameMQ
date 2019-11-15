package implementations;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import interfaces.Consumer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class GetSleepingAccountStatusConsumer implements Consumer {
    private JmsFactoryFactory rootFactory;
    private JmsConnectionFactory connectionFactory;
    private JMSContext context; //возможно кроме очереди ничего хранить не нужно - обдумать.
    private Queue output;
    private Queue input;
    private String message;

    public GetSleepingAccountStatusConsumer(String operationName){
        FileInputStream fis = null;
        Properties property = new Properties();
        String propFilePath = "src/main/resources/" + this.getClass().getSimpleName() + ".properties";
        try {
            fis = new FileInputStream(propFilePath);
            property.load(fis);
        }catch (FileNotFoundException fne){
            System.out.println("Sender properties file is missing!");
        }catch (IOException ioe){
            System.out.println("Unable to open sender properties file!");
        }finally {
            try {
                fis.close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        try {
            rootFactory = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            connectionFactory = rootFactory.createConnectionFactory();

            connectionFactory.setStringProperty(WMQConstants.WMQ_HOST_NAME, property.getProperty("host"));
            connectionFactory.setIntProperty(WMQConstants.WMQ_PORT, Integer.parseInt(property.getProperty("port")));
            connectionFactory.setStringProperty(WMQConstants.WMQ_CHANNEL, property.getProperty("channel"));
            connectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            connectionFactory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, property.getProperty("qManager"));
            connectionFactory.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
            connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);

            context = connectionFactory.createContext();
            output = context.createQueue("queue:///" + property.getProperty("qNameOut"));
            input = context.createQueue("queue:///" + property.getProperty("qNameIn"));

        }catch (JMSException jme){
            System.out.println("Unable to create MQ factories");
        }
    }


    public String generateResponse() {
        File messageFile = new File("src/main/resources/responses" + this.getClass().getSimpleName() + ".xml");
        Document xmlDocument = null;
        FileInputStream fileIS = null;
        try{
            fileIS = new FileInputStream(messageFile);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            xmlDocument = builder.parse(fileIS);
        }catch(FileNotFoundException fne){
            fne.printStackTrace();
        }catch(ParserConfigurationException pce){
            pce.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (SAXException sae){
            sae.printStackTrace();
        }finally {
            try{
                fileIS.close();
            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }




    }

    private HashMap<Node,String> getEvElements(NodeList nodeList,HashMap<Node,String> resMap){
        int len = nodeList.getLength();
        for (int num = 0;num < len;num++) {
            Node node = nodeList.item(num);
            NodeList childs = node.getChildNodes();

            if (childs.getLength() > 0){
                getEvElements(childs,resMap);
            }
            
        }
    }

    public void setInputSchema(String filename) {

    }

    public String recieveMessage() {
        JMSConsumer consumer = context.createConsumer(input);
        String receivedMessage = consumer.receiveBody(String.class, 15000);
        consumer.close();

        return receivedMessage;
    }

    public void sendMessage() {
        JMSProducer producer = context.createProducer();
        producer.send(output, message);
    }
}
