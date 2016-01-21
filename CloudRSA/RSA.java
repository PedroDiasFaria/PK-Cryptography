package CloudRSA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


//Cypher class
public class RSA {

    public static String ALGORITHM = "RSA";

    /*
       Key variables
    */
    public KeyPairGenerator key_par_gen = null;
    public KeyPair kp  = null;
    public PublicKey publicKey  = null;
    public PrivateKey privateKey  = null;
    public KeyFactory factory_rsa  = null;
    public RSAPublicKeySpec pub  = null;
    public RSAPrivateKeySpec priv  = null;
    public int keyLength = 1024;

    public Cipher cipher;

    //initializes all variables
    public RSA() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {

        key_par_gen = KeyPairGenerator.getInstance(ALGORITHM);
        key_par_gen.initialize(keyLength);

        kp = key_par_gen.generateKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();

        cipher = Cipher.getInstance(ALGORITHM);
    }

    public File encryptFile(File file) throws CryptoException {

        File encrypted = doCrypto(Cipher.ENCRYPT_MODE, publicKey, file);

        return encrypted;
    }

    public File decryptFile(File file) throws CryptoException {

        File decrypted = doCrypto(Cipher.DECRYPT_MODE, privateKey, file);;

        return decrypted;
    }

    private File doCrypto(int cipherMode, Key key, File inputFile) throws CryptoException {

        try {
            File outputFile = inputFile;
            cipher.init(cipherMode, key);

            FileInputStream inputStream = new FileInputStream(inputFile);

            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = blockCipher(inputBytes, cipherMode);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

            return outputFile;

        } catch (BadPaddingException | IllegalBlockSizeException
                | InvalidKeyException | IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }

    }



    //from http://coding.westreicher.org/?p=23
    //since RSA can't encrypt large data blocs, we need to divide them and encrypt at the same time
    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException{
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        //int length = (mode == Cipher.ENCRYPT_MODE)? 100 : 128;
        int length = (mode == Cipher.ENCRYPT_MODE) ? (keyLength / 8 ) - 11 : (keyLength / 8 );

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[(bytes.length > length ? length : bytes.length)];
        //byte[] buffer = new byte[length];

        for (int i=0; i< bytes.length; i++){

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)){
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn,scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i%length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn,scrambled);

        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix){
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i=0; i< prefix.length; i++){
            toReturn[i] = prefix[i];
        }
        for (int i=0; i< suffix.length; i++){
            toReturn[i+prefix.length] = suffix[i];
        }
        return toReturn;
    }
}
