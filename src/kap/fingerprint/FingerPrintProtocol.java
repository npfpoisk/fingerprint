package kap.fingerprint;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;


/** Виды команд отправляемых из 1с
 *  Команды.Команда = 0 - команда записи нового отпечатка в базу данных
 *  Команды.Команда = 1 - команда изменения отпечатка в базе данных
 *  Команды.Команда = 2 - команда проверки(наличие) отпечатка в базе данных
 *  Команды.Команда = 3 - команда изменения данных ФизЛица в базе данных
 *  Команды.Команда = 4 - команда пометки удаления отпечатка в базы данных
 *
 ******************** Описание команды Команда = 0 (Запись нового отпечатка) ********************
 * Шаг_1c - 1 = Создаем команду на стороне 1с - СоздатьОтпечаток, если ID_субд<>0 тогда ставим кнопке свойство ТолькоЧтение=Истина
 * Конф 1с генерирует xml файл с заполненными данными, согласно протокола
 *  [Владелец (Имя=1с,Дата=Год{4}Месяц{2}Дата{2})]
 *  [Команды (Команда=0,Фаза=0,Завершено=Ложь)]
 *  [ФизЛицо (Наименование=ФИО,Код=код1с,Фото={Требует доработки},ID_субд=0)]
 *  [Ошибки (Ошибка=Ложь)]
 *
 ** Шаг_1c - 2 = Конф 1с запускает bat файл который запускает java аплет
 *
 ** Шаш_java - 3 = Для java нет последовательности, аплет будет запуускаться с нуля каждый запуск, аплет должен определить статус для верного принятия действий
 *
 ** Шаг_java - 3.1 = Проверка
 * Аплет инициирует xml
 * Проверяем Наличие файла протокола
 * Проверяем Владелец.Имя==1с
 * Проверяем связь с СУБД
 * Проверяем структуру протокола Команды.Команда == 0
 *  Команды.Фаза == 0
 *   Проверяем на совпадение ФизЛицо.Наименование с ФИО в СУБД
 *   Проверяем на совпадение ФизЛицо.Код с Код в СУБД
 * При любых отклонениях делаем запись
 *  [Ошибки.Ошибка=Истина, Ошибки.Описание = текст ошибки]
 ** Шаг_java - 3.2 = Разрешение для сканирования отпечатка
 * Если ошибок нет заполняем
 *  [Владелец.Имя=java]
 *  [Команды.Команда=0, Команды.Фаза=1, Завершено=Ложь]
 *
 ** Шаг_1c - 4 = Проверка перед стартом 1-го сканирования отпечатка
 * Считываем на стороне 1с xml файл
 * Проверяем структуру Владелец.Имя=java, иначе выводим ошибку о не запуске java аплета
 * Проверяем структуру Ошибки.Ошибка
 *  Ложь - продолжаем работу
 *  Истина - выводим соообщение об ошибке останавливаем работу с аплетом
 * Проверяем Команды.Команда=0, Команды.Фаза=1
 *
 ** Шаг_1c - 5 = Старт 1-го сканирования отпечатка
 * Создаем команду старта 1-го сканирования
 * Выводим сообщение в приложении 1с "Положите палец на сканер отпечатков, после нажмите сканировать"
 * После анализа ответа по протоколу на ошибки, при нажатии на команду старта 1-го сканирования отпечатка, формируем ответ в аплет
 * [Владелец.Имя=1c]
 * [Команды.Команда=0, Команды.Фаза=1]
 * Отправляем команду запуска java аплета
 *
 ** Шаг_Java - 6 = Проверка
 * java Аплет инициирует xml
 * Проверяем Наличие файла протокола
 * Проверяем Владелец.Имя==1с
 * Проверяем связь с СУБД
 *
 ** Шаг_Java - 7 = Запуск 1-го сканирования
 * Проверяем структуру протокола Команды.Команда == 0
 *  Команды.Фаза == 1
 * Запускаем команды сканирования отпечатка пальца
 * Преобразуем и сохраняем отпечаток пальца в буфер1
 * Если в процессе обработки возникли ошибки передаем в файл протокола
 *  [Ошибки.Ошибка=Истина, Ошибки.Описание=описание ошибки]
 * Если операции прошли без ошибок заполняем файл протокола
 *  [Владелец.Имя=java]
 *  [Команды.Команда=0,Команды.Фаза=2]
 *
 ** Шаг_1c - 8 = Проверка перед стартом 2-го сканирования отпечатка
 * Считываем на стороне 1с xml файл
 * Проверяем структуру Владелец.Имя=java, иначе выводим ошибку о не запуске java аплета
 * Проверяем структуру Ошибки.Ошибка
 *  Ложь - продолжаем работу
 *  Истина - выводим соообщение об ошибке останавливаем работу с аплетом
 * Проверяем Команды.Команда=0, Команды.Фаза=2
 *
 ** Шаг_1c - 9 = Старт 2-го сканирования отпечатка
 * Создаем команду старта 2-го сканирования
 * Выводим сообщение в приложении 1с "Положите палец на сканер отпечатков, после нажмите сканировать"
 * После анализа ответа по протоколу на ошибки, при нажатии на команду старта 2-го сканирования отпечатка, формируем ответ в аплет
 * [Владелец.Имя=1c]
 * [Команды.Команда=0, Команды.Фаза=2]
 * Отправляем команду запуска java аплета
 *
 ** Шаг_Java - 6 = Проверка
 * java Аплет инициирует xml
 * Проверяем Наличие файла протокола
 * Проверяем Владелец.Имя==1с
 * Проверяем связь с СУБД
 *
 ** Шаг_Java - 10 = Сканирование 2-го отпечатка
 * Проверяем структуру протокола Команды.Команда == 0
 *  Команды.Фаза == 2
 * Запускаем команды сканирования отпечатка пальца
 * Преобразуем и сохраняем отпечаток пальца в буфер2
 * Если в процессе обработки возникли ошибки передаем в файл протокола
 *  [Ошибки.Ошибка=Истина, Ошибки.Описание=описание ошибки]
 *
 ** Шаг_Java - 11 = Создание шаблона отпечатка, сохранение в устройство и  сохранение в СУБД
 * Запустить процедуры изъятия шаблона отпечатка из устройства в массив и передача в СУБД для сохранения записи
 * Записать в СУБД поля данных из файла протокола
 * Если возникли ошибки передать файл протокола
 * [Ошибки.Ошибка=Истина, Ошибки.Описание=описание ошибки]
 * Запустить процедуры сохранения шаблона отпечатка в flash память устройства
 * Заполнитть файл протокола
 * [Владелец.Имя=java]
 * [Команды.Команда=0, Команды.Фаза=3]
 * [ФизЛицо.ID_субд=XX]
 * =================================================================================================

 *
 *
 *      Проверка на совпадение в СУБД
 *
*/

public class FingerPrintProtocol {

    private File fileXML;// ="./fpprotocol.xml";
    public Owner owner;
    public Check check;
    public Persona persona;
    public Status status;
    public Errors errors;

    public FingerPrintProtocol(String patchXML) throws Exception {

        this.fileXML = new File(patchXML);//"\\\\10.3.9.40\\Share\\fpprotocol.xml"
        //Проверяем существование файла
        if(!fileXML.exists()){
            //FingerPrintLog.error_fpprotocol("Ошибка fpprotocol","Файл = "+patchXML+", не был обнаружен");
            throw new SecurityException("Файл = "+patchXML+", не был обнаружен");
        }
        //Заполняем подклассы
        getStructureProtocol();
    }

    public class Owner      //Владелец
    {
        public String Name; //Имя
        public String Date; //Дата //20200122
        //
        public Owner(Node struct) throws Exception {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="Имя") this.Name=content;
                    else if (name=="Дата") this.Date=content;
                    else throw new Exception("Ошика формата fpprotocol.В структуре = Владелец, свойство = "+name+", не найдено в стандартах.");
                }
            }
        }
    }
    public class Check          //Проверка
    {
        public boolean C1;      //1с
        public boolean Java;    //Java
        public boolean DBMS;    //субд
        //
        public Check(Node struct) throws Exception {
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
                    else throw new Exception("Ошика формата fpprotocol.В структуре = Проверка, свойство = "+name+", не найдено в стандартах.");
                }
            }
        }
    }
    public class Persona    //ФизЛицо
    {
        public String Name; //Наименование
        public String Code; //Код
        public byte[] Foto; //Фото
        public int ID;      //ID
        //
        public Persona(Node struct) throws Exception {
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
                            throw new Exception("Ошика формата fpprotocol.В структуре = ФизЛицо, в свойстве = ID, неверно заполнено значение = "+content);
                        }
                    }
                    else throw new Exception("Ошика формата fpprotocol.В структуре = ФизЛицо, свойство = "+name+", не найдено в стандартах.");
                }
            }

        }
    }
    public class Status             //Состояние
    {
        public int Phase;           //Фаза
        public boolean WriteStatus; //Запись
        //
        public Status(Node struct) throws Exception {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    //String type=attributes.getNamedItem("type").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="Фаза") {
                        try {
                            this.Phase = Integer.parseInt(content);
                        }catch (NumberFormatException e){
                            throw new Exception("Ошика формата fpprotocol.В структуре = Состояние, в свойстве = Фаза, неверно заполнено значение = "+content);
                        }
                    }
                    else if (name=="Запись") this.WriteStatus=(content=="Истина"?true:false);
                    else throw new Exception("Ошика формата fpprotocol.В структуре = Состояние, свойство = "+name+", не найдено в стандартах.");
                }
            }
        }
    }
    public class Errors             //Ошибки
    {
        public boolean error;       //Ошибка
        public String Description;  //Описание
        //
        public Errors(Node struct) throws Exception {
            NodeList propertyList = struct.getChildNodes();
            for(int j = 0; j < propertyList.getLength(); j++) {
                Node property=propertyList.item(j);
                if (property.getNodeType() != Node.TEXT_NODE) {
                    NamedNodeMap attributes = property.getAttributes();
                    String name=attributes.getNamedItem("name").getNodeValue();
                    String type=attributes.getNamedItem("type").getNodeValue();
                    String content=property.getTextContent();
                    //
                    if (name=="Ошибка") this.error=(content=="Истина"?true:false);
                    else if (name=="Описание") this.Description=content;
                    else throw new Exception("Ошика формата fpprotocol.В структуре = Ошибки, свойство = "+name+", не найдено в стандартах.");
                }
            }
        }
    }

    private void getStructureProtocol() throws Exception {
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
                }else throw new Exception("Ошика формата fpprotocol.Наименование Структуры = "+name+", не найдено в стандартах.");
            }
        }
    }

}
