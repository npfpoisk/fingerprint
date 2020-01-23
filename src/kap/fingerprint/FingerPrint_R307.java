package kap.fingerprint;

public class FingerPrint_R307 extends FingerPrint  {

    public final byte FINGERPRINT_OK = 0x00;                // commad execution complete;

    private int adder; //По умолчанию adder = 0xffffffff

    private final byte CODE_READSYSPARA = 0x0f;
    private final byte CODE_SETSYSPARA = 0x0E;
    private final byte CODE_CONTROL = 0x17;

    private final byte CODE_genImg = 0x01;
    private final byte CODE_img2Tz = 0x02;
    private final byte CODE_regModel = 0x05;
    private final byte CODE_store = 0x06;

    private final byte CODE_templateNum = 0x1d;
    private final byte CODE_loadChar = 0x07;
    private final byte CODE_match = 0x03;
    private final byte CODE_hispeedsearch = 0x1b;//0x04;

    private final byte CODE_upChar = 0x08;
    private final byte CODE_downChar = 0x09;
    private final byte CODE_upImage = 0x0a;
    private final byte CODE_downImage = 0x0b;

    private final byte CODE_deleteChar = 0x0c;
    private final byte CODE_empty = 0x0d;


    FingerPrint_R307(FingerPrintLog fplog, int address) {
        super(fplog);
        this.adder = address;
    }

    FingerPrint_R307(FingerPrintLog fplog) {
        super(fplog);
        this.adder = 0xffffffff;
    }

    //============================================================================
    //* Сервисный функционал для выполнения операций над fingerprint
    //****************************************************************************

    /** Получаем количество сохраненных шаблонов в устройстве
     * SEND = EF01 FFFFFFFF 01 0003 1d 0021
     * Instruction code = 0x1d
     *
     * Header 				= 2 /{0,1}      /{0,1} /EF01
     * Module address 		= 4	/{2,3,4,5}  /FFFFFFFF
     * Package identifier	= 1 /{6}        /07
     * Package length		= 2 /{7,8}      /5
     * Confirmation code 	= 1 /{9}        /
     * Template number		= 2 /{10,11}    /N
     * Checksum				= 2 /{12,13}    /sum
     *
     * Confirmation code =
     * 00H - read complete
     * 01H	- error when receiving package;
     *
     * Return = 14 byte
     *
     * @return - int - количество шаблонов сохраненых во флеш памяти устройства
     */
    public int templateNum() {

        int readByteCount=14;
        writePacket(adder, new byte[]{CODE_templateNum},"write templateNum");
        byte[] read_data = readPacket(readByteCount,"read templateNum");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return -1;
        }
        //
        return hexToDec(read_data[10], read_data[11]);
    }

    /** Начать сканирование отпечатка, помещает отсканированный отпечаток в ImageBuffer
     *
     * SEND = EF01 FFFFFFFF 01 0003 01 0005
     *
     * Instruction code = 0x01
     *
     * RETURN:
     * 	Header 				= 2 /{0,1}		/EF01
     * 	Module address 		= 4	/{2,3,4,5} 	/FFFFFFFF
     * 	Package identifier	= 1 /{6}		/07
     * 	Package length		= 2 /{7,8}		/0003
     * 	Confirmation code 	= 1 /{9}		/
     * 	Checksum			= 2 /{10,11}	/sum
     *
     * Confirmation code =
     * 	00H - finger collection success
     * 	01H	- error when receiving package;
     * 	02H	- cant detect finger;
     * 	03H	- fail to collect finger
     *
     * Return = 12 byte
     */
    public boolean genImage() {

        int readByteCount=12;
        //
        writePacket(adder, new byte[]{CODE_genImg},"write genImage");
        byte[]read_data=readPacket(readByteCount,"read genImage");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    /** Полная очистка данных во flash памяти устройства
     *
     * SEND = EF01 FFFFFFFF 01 0003 0d 0011
     *
     * Instruction code = 0x06
     *
     * 	Header 				= 2 /{0,1}      /EF01
     * 	Module address 		= 4	/{2,3,4,5}  /FFFFFFFF
     * 	Package identifier	= 1 /{6}        /07
     * 	Package length		= 2 /{7,8}      /0003
     * 	Confirmation code 	= 1 /{9}        /
     * 	Checksum			= 2 /{10,11}    /sum
     *
     * Confirmation code =
     * 	00H - empty success;
     * 	01H	- error when receiving package;
     *
     * Return = 12 byte
     */
    public boolean empty()  {

        int readByteCount=12;
        //
        writePacket(adder,new byte[]{CODE_empty}, "write empty");
        byte[]read_data=readPacket(readByteCount,"read empty");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    /** Делает сравнение и поиск шаблона отпечатка, через параметры startPage и pageNum задается диапазон поиска в устройстве
     *
     * SEND= EF01 FFFFFFFF 01 0008 1b bufferID(1) startPage(2) pageNum(2) sum(2)
     *
     * Instruction code = 0x1B //04H - Search the finger library
     *
     * 	Header 				= 2 /{0,1}		/EF01
     * 	Module address 		= 4	/{2,3,4,5}	/FFFFFFFF
     * 	Package identifier	= 1 /{6}		/07
     * 	Package length		= 2 /{7,8}		/0007
     * 	Confirmation code 	= 1 /{9}		/
     * 	PageID				= 2 /{10,11}	/
     *  MatchScore			= 2 /{12,13}	/
     * 	Checksum			= 2 /{14,15}	/sum
     *
     * Confirmation code =
     * 	00H - found the matching finer;
     * 	01H	- error when receiving package;
     *  09H - No matching in the library (both the PageID and matching score are 0);
     *
     *  Return = 16 byte
     */
    public int search() {

        int readByteCount=16;
        byte[]read_data;
        //
        writePacket(adder, new byte[]{CODE_hispeedsearch,0x01,0,(byte)0x03,(byte)0xE8}, "write search");
        read_data=readPacket(readByteCount,"read search");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return -1;
        }
        //
        return hexToDec(read_data[10],read_data[11]);
    }

    /** Загружает шаблон отпечатка из flash памяти по указателю pageID в CharBuffer1 или CharBuffer2
     *
     * SEND = EF01 FFFFFFFF 01 0006 07 bufferID(1) pageID(2) sum(2)
     *
     * Instruction code = 0x06
     *
     * RETURN:
     * 	Header 				= 2 /{0,1} 		/EF01
     * 	Module address 		= 4	/{2,3,4,5} 	/FFFFFFFF
     * 	Package identifier	= 1 /{6} 		/07
     * 	Package length		= 2 /{7,8} 		/0003
     * 	Confirmation code 	= 1 /{9} 		/
     * 	Checksum			= 2 /{10,11} 	/sum
     *
     * Confirmation code =
     * 	00H - load success;
     * 	01H	- error when receiving package;
     * 	0cH	- error when reading template from library or the readout template is invalid;
     *  0bH - addressing PageID is beyond the finger library;
     *
     *  Return = 12 byte
     */
    public boolean loadChar(byte buf, int pageID) {

        int readByteCount=12;
        //
        writePacket(adder, new byte[]{CODE_loadChar, buf, (byte)(pageID >> 8),(byte)(pageID & 0xFF)}, "write loadChar");
        byte[]read_data=readPacket(readByteCount,"read loadChar");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    /** Сохранение шаблона отпечатка из CharBuffer1 или CharBuffer2 под номером pageID во flash память устройства
     *
     * SEND = EF01 FFFFFFFF 01 0006 06 bufferID(1) pageID(2) sum(2)
     *
     * Instruction code = 0x06
     *
     * RETURN:
     * 	Header 				= 2 /EF01
     * 	Module address 		= 4	/FFFFFFFF
     * 	Package identifier	= 1 /07
     * 	Package length		= 2 /0003
     * 	Confirmation code 	= 1 /
     * 	Checksum			= 2 /sum
     *
     * Confirmation code =
     * 	00H - storage success;
     * 	01H	- error when receiving package;
     * 	0bH	- addressing PageID is beyond the finger library;
     *  18H - error when writing Flash;
     *
     *  Return = 12 byte
     */
    public boolean store(byte buf, int pageID) {

        int readByteCount=12;
        //
        writePacket(adder, new byte[]{CODE_store, buf, (byte)(pageID >> 8),(byte)(pageID & 0xFF)},"write store");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            log.error(e.toString());
            return false;
        }
        byte[] read_data=readPacket(readByteCount,"read store");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    /** Команда выполняет объединения предварительных отпечатков пальцев из CharBuffer1 и CharBuffer2 и создает шаблон отпечатка помечая его обратно в CharBuffer1 и CharBuffer2
     *
     * SEND = EF01 FFFFFFFF 01 0003 05 0009
     *
     * Instruction code = 0x05
     *
     * 	Header 				= 2 /{0,1}		/EF01
     * 	Module address 		= 4	/{2,3,4,5}	/FFFFFFFF
     * 	Package identifier	= 1 /{6}		/07
     * 	Package length		= 2 /{7,8}		/0003
     * 	Confirmation code 	= 1 /{9}		/
     * 	Checksum			= 2 /{10,11}	/sum
     *
     * Confirmation code =
     * 	00H - operation success;
     * 	01H	- error when receiving package;
     * 	0aH	- fail to combine the character files. That�s, the character files don�t belong to one finger.
     *
     * Return = 12 byte
     */
    public boolean regModel() {

        int readByteCount=12;
        //
        writePacket(adder,new byte[]{CODE_regModel},"write regModel");
        byte[]read_data=readPacket(readByteCount, "read regModel");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    /** Команда сравнивает данные в CharBuffer1 и CharBuffer2 и выдает результат сравнения
     *
     * SEND = EF01 FFFFFFFF 01 0003 03 0007
     *
     * Instruction code = 0x03
     *
     * 	Header 				= 2	/{0,1}		/EF01
     * 	Module address 		= 4	/{2,3,4,5}	/FFFFFFFF
     * 	Package identifier	= 1 /{6}		/07
     * 	Package length		= 2 /{7,8}		/0005
     * 	Confirmation code 	= 1 /{9}		/
     *  Matching score 		= 2 /{10,11}	/
     * 	Checksum			= 2 /{12,13}	/sum
     *
     * Confirmation code =
     * 	00H - templates of the two buffers are matching!
     * 	01H	- error when receiving package;
     * 	08H	-  templates of the two buffers aren�t matching;
     *
     * Return = 14 byte
     */
    public int Match() {

        int readByteCount=14;
        //
        writePacket(adder, new byte[]{CODE_match},"write Match");
        byte[]read_data=readPacket(readByteCount,"read Match");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return -1;
        }
        //
        return hexToDec(read_data[10],read_data[11]);
    }

    /** Команда преобразует отпечаток пальца из imagebufer и помещает результат в CharBuffer1 или CharBuffer1
     *
     * SEND = EF01 FFFFFFFF 01 0004 02 bufNumber(1) sum(2)
     *
     * Instruction code = 0x02
     *
     * 	Header 				= 2	/{0,1}		/EF01
     * 	Module address 		= 4	/{2,3,4,5}	/FFFFFFFF
     * 	Package identifier	= 1 /{6}		/07
     * 	Package length		= 2 /{7,8}		/0003
     * 	Confirmation code 	= 1 /{9}		/
     * 	Checksum			= 2 /{10,11}	/sum
     *
     * Confirmation code =
     * 	00H - generate character file complete;
     * 	01H	- error when receiving package;
     * 	06H	- fail to generate character file due to the over-disorderly fingerprint image;
     * 	07H	- fail to generate character file due to lackness of character point or over-smallness of fingerprint image;
     * 	15H - fail to generate the image for the lackness of valid primary image
     *
     * Return = 12 byte
     */
    public boolean img2Tz(byte buf) {

        int readByteCount=12;
        //
        writePacket(adder, new byte[]{CODE_img2Tz, buf},"write img2Tz");
        byte[]read_data=readPacket(readByteCount,"read img2Tz");
        //Проверяем на ошибки
        if (read_data[9] != FINGERPRINT_OK)
        {
            log.error_cmd(read_data[9]);
            return false;
        }
        //
        return true;
    }

    //============================================================================
    //* Доступные функции для выполнения операций над fingerprint
    //****************************************************************************



}


