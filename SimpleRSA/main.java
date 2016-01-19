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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class main {
    public static void main(String[] args) throws IOException {

        RSA key;
        int[] keyLength = new int[]{32, 512, 1024}; //To test different sized keys

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter a string to encrypt:");
        String inputString = br.readLine();
        System.out.println("---------------------------------------------------------------");

        for(int i = 0; i<keyLength.length; i++ ){

            System.out.println("\n\nUsing a " + keyLength[i] + " length key:\n\n");
            key = new RSA(keyLength[i]);
            System.out.println(key);//calls the toString() and prints each of the values inside RSA class
            RSA.Cypher(key, inputString);
            System.out.println("---------------------------------------------------------------");
        }
    }
}
