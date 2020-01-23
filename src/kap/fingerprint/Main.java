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

        try {
            FingerPrintProtocol fpp=new FingerPrintProtocol("C:\\Users\\User\\Documents\\Projects\\fpprotocol.xml");
        } catch (Exception e) {
            FingerPrintLog.error_fpprotocol("Ошибка fpprotocol",e.toString());
            System.err.println(e.toString());
            //e.printStackTrace();
        }

    }

}

//        fp=new FingerPrint_R307(new FingerPrintLog("D:\\\\Share\\logFingerPrint.log",true,true));
//        if(!fp.openConnect("COM3")) return;
//        //
//        System.out.println(fp.templateNum());
//        //
//        fp.closeConnect();