/*
 * MD5 algorithm by Pedro Faria
 * Politechnika Krakowska im. Tadeusza Kościuszki
 * 1st Semester 2015/2016
 * Computer Science
 */

/*
 * To run, execute MD5.java. Make sure file example is on folder testFiles, and change name accordingly on variable: testFileName
 */

/*
 *   All strings/files codifications can be confirmed on: http://onlinemd5.com/
 */

package MD5;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class MD5 {

    private static final int INIT_A = 0x67452301;
    private static final int INIT_B = (int) 0xEFCDAB89L;
    private static final int INIT_C = (int) 0x98BADCFEL;
    private static final int INIT_D = 0x10325476;
    private static final String testFileName = "./src/MD5/testFiles/testFile.jpg";

    private static final int[] SHIFT_AMTS = {
            7, 12, 17, 22,
            5, 9, 14, 20,
            4, 11, 16, 23,
            6, 10, 15, 21
    };

    private static final int[] TABLE_T = new int[64];

    static {
        for (int i = 0; i < 64; i++)
            TABLE_T[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
    }

    public static byte[] MD5(byte[] message) {
        int messageLenBytes = message.length;
        int numBlocks = ((messageLenBytes + 8) >>> 6) + 1;
        int totalLen = numBlocks << 6;
        byte[] paddingBytes = new byte[totalLen - messageLenBytes];
        paddingBytes[0] = (byte) 0x80;

        long messageLenBits = (long) messageLenBytes << 3;
        for (int i = 0; i < 8; i++) {
            paddingBytes[paddingBytes.length - 8 + i] = (byte) messageLenBits;
            messageLenBits >>>= 8;
        }

        int a = INIT_A;
        int b = INIT_B;
        int c = INIT_C;
        int d = INIT_D;
        int[] buffer = new int[16];
        for (int i = 0; i < numBlocks; i++) {
            int index = i << 6;
            for (int j = 0; j < 64; j++, index++)
                buffer[j >>> 2] = ((int) ((index < messageLenBytes) ? message[index] : paddingBytes[index - messageLenBytes]) << 24) | (buffer[j >>> 2] >>> 8);
            int originalA = a;
            int originalB = b;
            int originalC = c;
            int originalD = d;
            for (int j = 0; j < 64; j++) {
                int div16 = j >>> 4;
                int f = 0;
                int bufferIndex = j;
                switch (div16) {
                    case 0:
                        f = (b & c) | (~b & d);
                        break;

                    case 1:
                        f = (b & d) | (c & ~d);
                        bufferIndex = (bufferIndex * 5 + 1) & 0x0F;
                        break;

                    case 2:
                        f = b ^ c ^ d;
                        bufferIndex = (bufferIndex * 3 + 5) & 0x0F;
                        break;

                    case 3:
                        f = c ^ (b | ~d);
                        bufferIndex = (bufferIndex * 7) & 0x0F;
                        break;
                }
                int temp = b + Integer.rotateLeft(a + f + buffer[bufferIndex] + TABLE_T[j], SHIFT_AMTS[(div16 << 2) | (j & 3)]);
                a = d;
                d = c;
                c = b;
                b = temp;
            }

            a += originalA;
            b += originalB;
            c += originalC;
            d += originalD;
        }

        byte[] md5 = new byte[16];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int n = (i == 0) ? a : ((i == 1) ? b : ((i == 2) ? c : d));
            for (int j = 0; j < 4; j++) {
                md5[count++] = (byte) n;
                n >>>= 8;
            }
        }
        return md5;
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02X", b[i] & 0xFF));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {

        System.out.println("NOW HASHING STRING EXAMPLES...");
        String[] testStrings = {
                "",
                "a",
                "abc",
                "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "Pedro Faria"
        };

        for (String s : testStrings)
            System.out.println("0x" + toHexString(MD5(s.getBytes())) + " <==> \"" + s + "\"");

        System.out.println("\n\nNOW HASHING A FILE...");
        //The file must be on the testFiles folder
        Path path = Paths.get(testFileName);
        byte[] data = Files.readAllBytes(path);
        //FileInputStream fis = new FileInputStream(new File("testFile.ppt"));
        System.out.println("0x" + toHexString(MD5(data)) + " <==> \"" + testFileName + "\"");

        System.out.println("\n\nNOW HASHING 2 DIFFERENT HEX STRINGS WITH COLLISION...");
        String hexCollision1 = "4dc968ff0ee35c209572d4777b721587d36fa7b21bdc56b74a3dc0783e7b9518afbfa200a8284bf36e8e4b55b35f427593d849676da0d1555d8360fb5f07fea2";
        byte[] hexBytes = DatatypeConverter.parseHexBinary(hexCollision1);
        System.out.println("0x" + toHexString(MD5(hexBytes)) + " <==> \"" + hexCollision1 + "\"");
        String hexCollision2 = "4dc968ff0ee35c209572d4777b721587d36fa7b21bdc56b74a3dc0783e7b9518afbfa202a8284bf36e8e4b55b35f427593d849676da0d1d55d8360fb5f07fea2";
        hexBytes = DatatypeConverter.parseHexBinary(hexCollision2);
        System.out.println("0x" + toHexString(MD5(hexBytes)) + " <==> \"" + hexCollision2 + "\"");
        System.out.println("\nThey are different in: \n" +
                "4dc968ff0ee35c209572d4777b721587d36fa7b21bdc56b74a3dc0783e7b9518afbfa20 [0] a8284bf36e8e4b55b35f427593d849676da0d1 [5] 55d8360fb5f07fea2\n" +
                "4dc968ff0ee35c209572d4777b721587d36fa7b21bdc56b74a3dc0783e7b9518afbfa20 [2] a8284bf36e8e4b55b35f427593d849676da0d1 [d] 55d8360fb5f07fea2");
        return;
    }

}