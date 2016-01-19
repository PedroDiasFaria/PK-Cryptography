import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

/**
 * Created by Pedro Faria on 19-Nov-15.
 */
//Cypher class
public class RSA {
	
    /*
       Key variables
    */
    private KeyPairGenerator key_par_gen = null;
    private KeyPair kp  = null;
    private PublicKey publicKey  = null;
    private PrivateKey privateKey  = null;
    private KeyFactory factory_rsa  = null;
    private RSAPublicKeySpec pub  = null;
    private RSAPrivateKeySpec priv  = null;

    /*
       Key values
    */
    private BigInteger n;
    private BigInteger e;


    public RSA(){
    	
        setKey_par_gen();
        setKp();
    }

    //Getters & Setters
    public KeyPairGenerator getKey_par_gen() {
        return key_par_gen;
    }

    public void setKey_par_gen() {
            try {
				this.key_par_gen = KeyPairGenerator.getInstance("RSA");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    public KeyPair getKp() {
        return kp;
    }	

    public void setKp() {
        this.kp = getKey_par_gen().genKeyPair();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public KeyFactory getFactory_rsa() {
        return factory_rsa;
    }

    public void setFactory_rsa(KeyFactory factory_rsa) {
        this.factory_rsa = factory_rsa;
    }

    public RSAPublicKeySpec getPub() {
        return pub;
    }

    public void setPub(RSAPublicKeySpec pub) {
        this.pub = pub;
    }

    public RSAPrivateKeySpec getPriv() {
        return priv;
    }

    public void setPriv(RSAPrivateKeySpec priv) {
        this.priv = priv;
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }
}
