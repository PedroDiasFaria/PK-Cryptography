/*
 * Simple RSA by Pedro Faria
 * Politechnika Krakowska im. Tadeusza Kościuszki
 * 1st Semester 2015/2016
 * Computer Science
 */

/*
 * To run, execute main.java and input a String to encrypt
 */

package SimpleRSA;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;


public class RSA {
    //Some definitions
    private final static BigInteger one      = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();

    //RSA Keys
    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;

    //Cypher variables
    private static BigInteger inputBI;
    private static String decryptedString;



    public static void Cypher(RSA key, String inputString) throws IOException {

        byte[] inputStringByteArray = stringToBytes(inputString);
        inputBI = new BigInteger(inputStringByteArray);

        BigInteger encrypt = key.encrypt(inputBI);
        BigInteger decrypt = key.decrypt(encrypt);
        System.out.println("message(string)       = " + inputString);
        System.out.println("message(BigInteger)   = " + inputBI);
        System.out.println("encrypted             = " + encrypt);
        System.out.println("decrypted(BigInteger) = " + decrypt);

        decryptedString = bytesToString(decrypt.toByteArray());
        System.out.println("decrypted(string)     = " + decryptedString);
    }

    // generate an N-bit (roughly) public and private key
    RSA(int N) {
        BigInteger p = BigInteger.probablePrime(N, random);
        BigInteger q = BigInteger.probablePrime(N, random);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

        modulus    = p.multiply(q);
        publicKey  = new BigInteger("65537");     // common value in practice = 2^16 + 1
        //The value 65537 is the commonly used exponent of RSA keys.
        privateKey = publicKey.modInverse(phi);

    }

    BigInteger encrypt(BigInteger message) {
        System.out.println("Now encrypting \"" + message + "\"");
        return message.modPow(publicKey, modulus);
    }

    BigInteger decrypt(BigInteger encrypted) {
        System.out.println("Now decrypting \"" + encrypted + "\"");
        return encrypted.modPow(privateKey, modulus);
    }

    public String toString() {
        String s = "";
        s += "public    = " + publicKey  + "\n";
        s += "private   = " + privateKey + "\n";
        s += "modulus   = " + modulus    + "\n";
        return s;
    }

    //Not used, BigInteger has modPow already implemented
    private static BigInteger findGCD(BigInteger number1, BigInteger number2) {
        //base case
        if(number2.equals(0)){
            return number1;
        }
        return findGCD(number2, number1.mod(number2));
    }

    static String bytesToString(byte[] b) throws UnsupportedEncodingException {
        return new String(b, "UTF-8");
    }

    static byte[] stringToBytes(String s) {
        return s.getBytes();
    }

}