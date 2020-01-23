package kap.fingerprint;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FingerPrintLog {

    //Класс эмитирующий структуру для сохранения кода ошибки и сообщения об ошибке
    public class StructError{
        public final byte error;
        public final String message;

        public StructError(byte error, String message ) {
            this.error = error;
            this.message = message;
        }
    }

    public final StructError error_0x01 = new StructError( (byte)0x01,"Error when receiving data package");
    public final StructError error_0x02 = new StructError( (byte)0x02,"No finger on the sensor");
    public final StructError error_0x03 = new StructError( (byte)0x03,"Fail to enroll the finger");
    public final StructError error_0x06 = new StructError( (byte)0x06,"Fail to generate character file due to the over-disorderly fingerprint image");
    public final StructError error_0x07 = new StructError( (byte)0x07,"Fail to generate character file due to lackness of character point or over-smallness of fingerprint image");
    public final StructError error_0x08 = new StructError( (byte)0x08,"Finger doesn’t match");
    public final StructError error_0x09 = new StructError( (byte)0x09,"Fail to find the matching finger");
    public final StructError error_0x0A = new StructError( (byte)0x0A,"Fail to combine the character files");
    public final StructError error_0x0B = new StructError( (byte)0x0B,"Addressing PageID is beyond the finger library");
    public final StructError error_0x0C = new StructError( (byte)0x0C,"Error when reading template from library or the template is invalid");
    public final StructError error_0x0D = new StructError( (byte)0x0D,"Error when uploading template");
    public final StructError error_0x0E = new StructError( (byte)0x0E,"Module can’t receive the following data packages");
    public final StructError error_0x0F = new StructError( (byte)0x0F,"Error when uploading image");
    public final StructError error_0x10 = new StructError( (byte)0x10,"Fail to delete the template");
    public final StructError error_0x11 = new StructError( (byte)0x11,"Fail to clear finger library");
    public final StructError error_0x13 = new StructError( (byte)0x13,"Wrong password!");
    public final StructError error_0x15 = new StructError( (byte)0x15,"Fail to generate the image for the lackness of valid primary image");
    public final StructError error_0x18 = new StructError( (byte)0x18,"Error when writing flash");
    public final StructError error_0x1A = new StructError( (byte)0x1A,"Invalid register number");

    public boolean writeByte = false;    //Выводить данные передаваемых байт по COM порту
    public boolean writeLogFile = false; //Делать записи в лог файл
    public boolean sendLogEmail = false; //Отправлять сообщения по почте
    public String patchLogFile;          //Путь к лог файлу

    public FingerPrintLog(String patchLogFile,boolean writeLogFile, boolean writeByte) {

        this.patchLogFile = patchLogFile;
        this.writeByte = writeByte;
        this.writeLogFile = writeLogFile;
    }

    void error_openConnect(String message){

        if(writeLogFile) writeLogFile("ОШИБКА соединения",message);
    }

    void write_byte(String text, byte[] data){

        if(writeLogFile && writeByte) writeLogFile(text,data);
    }

    void error_readCOM(String message){

        if(writeLogFile) writeLogFile("ОШИБКА чтения из СOM порта",message);
    }

    void error_writeCOM(String message){

        if(writeLogFile) writeLogFile("ОШИБКА записи в СOM порт",message);
    }

    void error(String message){
        if(writeLogFile) writeLogFile("ОШИБКА",message);
    }

    void error_cmd(byte cod){
        if(cod==error_0x01.error) {
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x01.message);
        }else if(cod==error_0x02.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x02.message);
        }else if(cod==error_0x03.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x03.message);
        }else if(cod==error_0x06.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x06.message);
        }else if(cod==error_0x07.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x07.message);
        }else if(cod==error_0x08.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x08.message);
        }else if(cod==error_0x09.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x09.message);
        }else if(cod==error_0x0A.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x0A.message);
        }else if(cod==error_0x0B.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x0B.message);
        }else if(cod==error_0x0C.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x0C.message);
        }else if(cod==error_0x15.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x15.message);
        }else if(cod==error_0x18.error){
            if(writeLogFile) writeLogFile("ОШИБКА операции",error_0x18.message);
        }else{
            if(writeLogFile) writeLogFile("ОШИБКА операции","Не известный код ошибки = "+String.format("%02x",cod));
        }
    }
    //============================================================================
    //* Работы с протоколом fpprotocol
    //****************************************************************************
    public static void error_fpprotocol(String theme, String message){

    }

    //============================================================================
    //* Запись логов
    //****************************************************************************
    private void writeLogFile(String theme, byte[]mas_finger) {

        FileWriter fw = null;
        Date date = new Date();
        SimpleDateFormat fDate = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        try {
            fw = new FileWriter(this.patchLogFile, true);
            fw.write(fDate.format(date)+" - "+theme+":\n");
            for (int i = 0; i < mas_finger.length; i++)
                fw.write("0x" + String.format("%02x", mas_finger[i])+",");
            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLogFile(String theme, String text) {

        FileWriter fw = null;
        Date date = new Date();
        SimpleDateFormat fDate = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        try {
            fw = new FileWriter(this.patchLogFile, true);
            fw.write(fDate.format(date)+" - "+theme+" - "+text+"\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
