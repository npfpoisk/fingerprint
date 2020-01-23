package kap.fingerprint;

//Импортируем библиотеку для работы с COM портом
import jssc.SerialPort;
import jssc.SerialPortException;
/*
//Для подключения соытий
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
*/


public class FingerPrint {

    private int FINGERPRINT_STARTCODE = 0xEF01;

    public byte[] finger;   // Хранит массив с полученным шаблоном отпечатка пальца
    private static SerialPort serialPort;
    FingerPrintLog log;

    /**Конструктор*/
    FingerPrint(FingerPrintLog log) {
        this.log = log;
    }

    //============================================================================
    //* Функционал для выполнения операций над COM портом
    //****************************************************************************

    /**Выполняем соединение с COM портом
     *
     * @param COM_port - Пример="COM5"
     * @return - true открыто соединение, false - произошла ошибка
     */
    public boolean openConnect(String COM_port)  {

        serialPort = new SerialPort(COM_port);// Пример"COM5"
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Для подключения события
            //serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
        } catch (SerialPortException e) {
            log.error_openConnect(e.toString());
            return false;
        }
        return true;
    }

    /**Проверка на подключение COM порта
     * @return true - порт подключен, false - порт не подключен
     */
    public boolean isOpen(){
        return serialPort.isOpened();
    }

    /** Закрываем открытый COM порт
    */
    public void closeConnect() {
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            //log
            //System.err.println(e);
        }
    }




    //============================================================================
    //* Функции работы с передачей байтов данных по COM порту
    //****************************************************************************

    /** Считываем байты данных из COM порта
     *
      * @param byteCount - количество считываемых байт из COM порта
     * @param text - строка для debug параметра, для записи передаваймых байт в лог
     * @return - возвращает ответ, массив считанных байт из COM порта
     */
    protected byte[] readPacket(int byteCount,String text) {

        byte[]mas_bytes=null;
        try {
            mas_bytes=serialPort.readBytes(byteCount);
        }
        catch (SerialPortException e) {
            log.error_readCOM(e.toString());
        }
        //Передаем в лог массив байс считанных из COM порта
        log.write_byte(text,mas_bytes);
        //
        return mas_bytes;
    }

    /** Передаем байты данных в COM порт
     *
      * @param packet - массив с байтами для отправки команды
     * @param text - строка для debug параметра, для записи передаваймых байт в лог
     */
    protected void writePacket(int adder, byte[] packet, String text) {

        byte packetType=(byte)0x01;
        int length = packet.length + 2;
        byte[] data = new byte[packet.length + 11];

        data[0] = (byte) 0xEF; // (byte) (FINGERPRINT_STARTCODE >> 8);
        data[1] = (byte) 0x01; // (byte) FINGERPRINT_STARTCODE;
        data[2] = (byte) ((adder>>24)&0xff);
        data[3] = (byte) ((adder>>16)&0xff);
        data[4] = (byte) ((adder>>8)&0xff);
        data[5] = (byte) (adder&0xff);
        data[6] = (byte) packetType;
        data[7] = (byte) (length >> 8);
        data[8] = (byte) length;
        //Вычесляем проверочную сумму
        int sum = (length >> 8) + (length & 0xFF) + packetType;
        for (int i = 0; i < packet.length; i++) {
            data[9 + i] = packet[i];
            sum += ((int) packet[i]) & 0xFF;
        }
        data[9 + packet.length] = (byte) (sum >> 8);
        data[10 + packet.length] = (byte) (sum & 0xFF);
        //Передаем в лог массив передаваемых данные в COM порт
        log.write_byte(text,data);
        //
        try {
            serialPort.writeBytes(data);
        }
        catch (SerialPortException e) {
            log.error_writeCOM(e.toString());
        }
    }

    /**Преобразовать из 16-ного в 10-ное число
     *
     * @param byte1 - первый разряд 16-ного числа
     * @param byte2 - второй разряд 16-ного числа
     * @return - Получить десятичное число
     */
    static int hexToDec(byte byte1, byte byte2) {
        int tmpDec = byte1 << 8;
        tmpDec |= byte2;
        return tmpDec;
    }

    //============================================================================
    //* События
    //****************************************************************************

    /* Функция которая срабатывает по событию если оно активировано шапке
    private class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //�������� ����� �� ����������, ������������ ������ � �.�.
                    //String data = serialPort.readString(event.getEventValue());
                    byte[]mas_bytes=serialPort.readBytes(event.getEventValue());
                    //
                    //
                    String patchFile="D:\\Admin_Share\\log.txt";
                    FileWriter fw=new FileWriter(patchFile, true);
                    for(int i=0;i<mas_bytes.length;i++)
                        fw.write("(byte)0x"+String.format("%02x", mas_bytes[i])+",");
                    fw.close();
                    //
                    //
                    //serialPort.writeString("Get data");
                }
                catch (SerialPortException | IOException ex) {
                    System.out.println(ex);
                }
            }
        }
    }*/
}

