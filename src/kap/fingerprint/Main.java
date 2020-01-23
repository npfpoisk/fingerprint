package kap.fingerprint;

import jssc.SerialPortException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Main {

    static FingerPrint_R307 fp;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        // Получение фабрики, чтобы после получить билдер документов.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Получили из фабрики билдер, который парсит XML, создает структуру Document в виде иерархического дерева.
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Запарсили XML, создав структуру Document. Теперь у нас есть доступ ко всем элементам, каким нам нужно.
        Document document = builder.parse(new File("\\\\10.3.9.40\\Share\\fpprotocol.xml"));
        //
        Element root = document.getDocumentElement();
        NodeList parameterList = root.getElementsByTagName("Structure");
        //
        for (int i = 0; i < parameterList.getLength(); i++) {
            Node struct = parameterList.item(i);
            if (struct.getNodeType() != Node.TEXT_NODE) {
                NamedNodeMap attributes = struct.getAttributes();
                String name=attributes.getNamedItem("name").getNodeValue();
                //
                //
                NodeList propertyList = struct.getChildNodes();
                for(int j = 0; j < propertyList.getLength(); j++) {
                    Node property=propertyList.item(j);
                    if (property.getNodeType() != Node.TEXT_NODE) {
                        attributes = property.getAttributes();
                        name=attributes.getNamedItem("name").getNodeValue();
                        String type=attributes.getNamedItem("type").getNodeValue();
                        String content=property.getTextContent();
                        //
                        //
                    }
                }
//
            }
        }

    }

}

//        fp=new FingerPrint_R307(new FingerPrintLog("D:\\\\Share\\logFingerPrint.log",true,true));
//        if(!fp.openConnect("COM3")) return;
//        //
//        System.out.println(fp.templateNum());
//        //
//        fp.closeConnect();