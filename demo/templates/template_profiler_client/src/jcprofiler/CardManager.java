package jcprofiler;

import java.util.List;
import javax.smartcardio.*;

/**
 *
 * @author Petr Svenda
 */
public class CardManager {
    CardTerminal m_terminal = null;
    CardChannel m_channel = null;
    Card m_card = null;
    Long m_lastTransmitTime = (long) 0;
    
    public final static byte OFFSET_CDATA = 5;
    public final static short SW_NO_ERROR = (short) 0x9000;

    public boolean ConnectToCard(byte[] appletAID) throws Exception {
        // Try all readers, connect to fisrt card
        List terminalList = GetReaderList();

        if (terminalList.isEmpty()) {
            System.out.println("No terminals found");
        }

        //List numbers of Card readers
        for (int i = 0; i < terminalList.size(); i++) {
            System.out.println(i + " : " + terminalList.get(i));
            m_terminal = (CardTerminal) terminalList.get(i);
            if (m_terminal.isCardPresent()) {
                m_card = m_terminal.connect("*");
                System.out.println("card: " + m_card);
                m_channel = m_card.getBasicChannel();

                //reset the card
                ATR atr = m_card.getATR();
                System.out.println(bytesToHex(m_card.getATR().getBytes()));
                
                System.out.println("Selecting applet...");
                CommandAPDU cmd = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, appletAID);
                ResponseAPDU response = transmit(cmd);
                
                return true;
            }
        }

        return false;
    }

    public void DisconnectFromCard() throws Exception {
        if (m_card != null) {
            m_card.disconnect(false);
            m_card = null;
        }
    }
    
    public List GetReaderList() {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List readersList = factory.terminals().list();
            return readersList;
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
            return null;
        }
    }
    
    public ResponseAPDU transmit(CommandAPDU cmd) throws CardException {
        log(cmd);

        long elapsed = -System.currentTimeMillis();
        ResponseAPDU response = m_channel.transmit(cmd);
        elapsed += System.currentTimeMillis();
        m_lastTransmitTime = elapsed;

        log(response, m_lastTransmitTime);

        return response;
    }

    private void log(CommandAPDU cmd) {
        System.out.printf("--> %s\n", toHex(cmd.getBytes()),
                cmd.getBytes().length);
    }

    private void log(ResponseAPDU response, long time) {
        String swStr = String.format("%02X", response.getSW());
        byte[] data = response.getData();
        if (data.length > 0) {
            System.out.printf("<-- %s %s (%d) [%d ms]\n", toHex(data), swStr,
                    data.length, time);
        } else {
            System.out.printf("<-- %s [%d ms]\n", swStr, time);
        }
    }
    
    public String byteToHex(byte data) {
        StringBuilder buf = new StringBuilder();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    public char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public String bytesToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            buf.append(byteToHex(data[i]));
            buf.append(" ");
        }
        return (buf.toString());
    }
    
    public static byte[] hexStringToByteArray(String s) {
        String sanitized = s.replace(" ", "");
        byte[] b = new byte[sanitized.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(sanitized.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }    
    
    public static String toHex(byte[] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    public static String toHex(byte[] bytes, int offset, int len) {
        String result = "";

        for (int i = offset; i < offset + len; i++) {
            result += String.format("%02X", bytes[i]);
        }

        return result;
    }    
}
