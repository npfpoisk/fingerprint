package kap.fingerprint;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;

public class FingerPrintProtocol {

    private File fileXML;// ="./fpprotocol.xml";
    Owner owner;
    Check check;
    Persona persona;
    Status status;
    Errors errors;

    public FingerPrintProtocol(String patchXML) {

        this.fileXML = new File(patchXML);//"\\\\10.3.9.40\\Share\\fpprotocol.xml"
        //Проверяем можем ли мы исзменять файл
        if(!fileXML.canWrite()) {
            FingerPrintLog.error_protocolXML("Ошибка fpprotocol","Файл = "+patchXML+", нельзя изменить");
            throw new SecurityException("Файл = "+patchXML+", нельзя изменить");
        }
        //Проверяем существование файла
        if(!fileXML.exists()){
            FingerPrintLog.error_protocolXML("Ошибка fpprotocol","Файл = "+patchXML+", не был обнаружен");
            throw new SecurityException("Файл = "+patchXML+", не был обнаружен");
        }
        //Заполняем подклассы
    }

    public class Owner // Владелец
    {
        public String Name;
        public String Date; //20200122
        //
        public Owner(Node struct) {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    String type=attributes.getNamedItem("type").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="Имя") this.Name=content;
                    else if (name=="Дата") this.Date=content;
                    else ;//ОШИБКА
                }
            }
        }
    }
    public class Check //Проверка
    {
        public boolean C1;
        public boolean Java;
        public boolean DBMS;
        //
        public Check(Node struct) {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    String type=attributes.getNamedItem("type").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="1с") this.C1=(content=="Истина"?true:false);
                    else if (name=="java") this.Java=(content=="Истина"?true:false);
                    else if (name=="субд") this.DBMS=(content=="Истина"?true:false);
                    else ;//ОШИБКА
                }
            }
        }
    }
    public class Persona //ФизЛицо
    {
        public String Name;
        public String Code;
        public byte[] Foto;
        public int ID;
        //

        public Persona(Node struct) {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    String type=attributes.getNamedItem("type").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="Наименование") this.Name=content;
                    else if (name=="Код") this.Code=content;
                    else if (name=="Фото") ;//this.Foto=content;
                    else if (name=="ID") {
                        try {
                            this.ID = Integer.parseInt(content);
                        }catch (NumberFormatException e){
                            //ОШИБКА
                        }
                    }
                    else ;//ОШИБКА
                }
            }

        }
    }
    public class Status //Состояние
    {
        public int Phase;
        public boolean WriteStatus;
    }
    public class Errors //Ошибка
    {
        public int error;
        public String Description;
    }

    private void getStructureProtocol() throws IOException, SAXException, ParserConfigurationException {
        // Получение фабрики, чтобы после получить билдер документов.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Получили из фабрики билдер, который парсит XML, создает структуру Document в виде иерархического дерева.
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Запарсили XML, создав структуру Document. Теперь у нас есть доступ ко всем элементам, каким нам нужно.
        Document document = builder.parse(fileXML);
        Element root = document.getDocumentElement();
        NodeList parameterList = root.getElementsByTagName("Structure");

        for (int i = 0; i < parameterList.getLength(); i++) {
            Node struct = parameterList.item(i);
            if (struct.getNodeType() != Node.TEXT_NODE) {
                NamedNodeMap attributes = struct.getAttributes();
                String name=attributes.getNamedItem("name").getNodeValue();
                //Определяем подкласс протокола
                if (name=="Владелец"){
                    owner=new Owner(struct);
                }else if (name=="Проверка"){
                    check=new Check(struct);
                }else if (name=="ФизЛицо"){
                    persona=new Persona(struct);
                }else if (name=="Состояние"){
                    status=new Status(struct);
                }else if (name=="Ошибки"){
                    errors=new Errors(struct);
                }else{
                    //Ошибка
                }//
            }
        }
    }

}
