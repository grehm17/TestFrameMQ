package implementations;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import interfaces.Sender;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.jms.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Properties;

public class GetSleepingAccountStatusSender implements Sender {
    private JmsFactoryFactory rootFactory;
    private JmsConnectionFactory connectionFactory;
    private JMSContext context; //возможно кроме очереди ничего хранить не нужно - обдумать.
    private Queue output;
    private Queue input;
    private String message;

    public GetSleepingAccountStatusSender(String operationName) {
        FileInputStream fis = null;
        Properties property = new Properties();
        String propFilePath = "src/main/resources/" + operationName + "Sender.properties";
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

    public void sendMessage(){
        JMSProducer producer = context.createProducer();
        producer.send(output, message);
    }

    public void setInputSchema(String filename) {

    }

    public String recieveMessage(){
        JMSConsumer consumer = context.createConsumer(input);
        String receivedMessage = consumer.receiveBody(String.class, 15000);
        consumer.close();

        return receivedMessage;
    }

    public boolean checkResult(String message){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document xmlDoc = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(message));
            xmlDoc = builder.parse(is);
        }catch (ParserConfigurationException pce){
            pce.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }catch (SAXException sae){
            sae.printStackTrace();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "";
        NodeList nodeList = null;
        try {
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
        }catch(XPathExpressionException xpe){
            xpe.printStackTrace();
        }
        String testVal = nodeList.item(0).getNodeValue();
        if (testVal.equals("111")){
            return true;
        }else{
            return false;
        }
    }

    public void loadMessage(String filepath){
        File messageFile = new File(filepath);
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

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));

            String xmlString = writer.getBuffer().toString();
            System.out.println(xmlString);
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }

    }
}
