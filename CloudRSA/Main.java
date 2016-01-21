package CloudRSA;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * Steps:
 * 1. Select number of senders
 * 2. Divide files between each sender
 * 3. Each sender sends his chunk to the cloud encrypted
 * 4. Cloud keeps chunks encrypted
 * 5. Receiver asks for a file
 * 6. Cloud sends chunks to receiver, who then decrypts and merge
 */

public class Main {

    public static String rootPath = "./src/CloudRSA/";
	public static String filesFolder = rootPath + "FilesAvailable/";
	public static String chunksFolder = rootPath + "ChunksFolder/"; //chunks still encrypted are stored here
	public static String cloudFolder = rootPath + "CloudFolder/";
	public static String receiverFolder = rootPath + "ReceiverFolder/";
    public static String fileName = "testFile.jpg";//"testFile.txt";
    
    public static int nrSenders = 2;
    
    static RSA rsa = null;
    static Receiver receiver = null;

    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {

        //initialize all agents on the system
    	initializeSystem();

    	//testFileSplitter();

        //split all the files
        splitAllFiles();

        //send them to the cloud
        sendToCloud();

        //receiver asks for a file in cloud
        retrieveFile();
    }

    private static void initializeSystem() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        rsa = new RSA();
        receiver = new Receiver();

        //create folders if they don't exist
        //delete content if they do
        File filesDir = new File(filesFolder);
        filesDir.mkdir();
        File chunksDir = new File(chunksFolder);
        deleteDir(chunksDir);
        chunksDir.mkdir();
        File cloudDir = new File(cloudFolder);
        deleteDir(cloudDir);
        cloudDir.mkdir();
        File receiverDir = new File(receiverFolder);
        deleteDir(receiverDir);
        receiverDir.mkdir();
    }

    static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    private static void testFileSplitter() throws IOException{
    	splitFile(new File(filesFolder + fileName));
    	    	
    	List<File> lf = listOfFilesToMerge(new File(chunksFolder + fileName));
    	
    	if(!lf.isEmpty())
    		System.out.println(lf.toString());
    	
    	String testReceiverFileName = receiver.receiveFile(getCloudFiles());
    	
    	mergeFiles(lf, new File(receiverFolder + testReceiverFileName));
    }

    private static void splitAllFiles() throws IOException {

        File folder = new File(filesFolder);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName() + " being split...");
                splitFile(new File(filesFolder + listOfFiles[i].getName()));
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        System.out.println("All files split in " + nrSenders + " chunks.");
    }

    //this function will encrypt the chunks to the cloud
    private static void sendToCloud() throws CryptoException {
        System.out.println("Encrypting and ending chunks to cloud folder...");
        File chunksF = new File(chunksFolder);
        File[] listOfChunks = chunksF.listFiles();

        encryptChunks(Arrays.asList(listOfChunks));
        for (File chunk : listOfChunks){
            File oldFile = chunk;
            chunk.renameTo(new File(cloudFolder + chunk.getName()));
            oldFile.delete();
        }

        System.out.println("Chunks encrypted in cloud");
    }

    private static void retrieveFile() throws IOException, CryptoException {

        String receiverFileName = receiver.receiveFile(getCloudFiles());

        List<File> lf = listOfFilesToMerge(new File(cloudFolder + receiverFileName));

        lf = receiveChunks(lf);
        decryptChunks(lf);

        System.out.println("Now merging chunks...");
        mergeFiles(lf, new File(receiverFolder + receiverFileName));
        System.out.println("File Received. Now exiting!");
    }

    //Simulates the receiving from the cloud to a temporary chunk folder
    private static List<File> receiveChunks(List<File> lf) throws IOException {

        for (File chunk: lf) {
            Files.copy(chunk.toPath(), new File(chunksFolder + chunk.getName()).toPath());
        }

        File chunksF = new File(chunksFolder);
        File[] lf2 = chunksF.listFiles();
        return Arrays.asList(lf2);
    }

    private static void decryptChunks(List<File> lf) throws CryptoException {

        for (File chunk : lf) {
            System.out.println("Decrypting chunk: " + chunk.getName() + " ...");
            chunk = rsa.decryptFile(chunk);
        }

        System.out.println("All chunks decrypted");
    }

    private static void encryptChunks(List<File> lf) throws CryptoException {
        for (File chunk : lf){
            System.out.println("Encrypting chunk: " + chunk.getName() + " ...");
            chunk = rsa.encryptFile(chunk);
        }

        System.out.println("All chunks encrypted");
    }

	//Content of the cloud
	public static List<String> getCloudFiles(){		
		
		List<String> fileNames = new ArrayList<String>();


		File[] files = new File(cloudFolder).listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		for (File file : files) {
		    if (file.isFile()) {
		    	String tempName = file.getName();
		    	
		    	String fileName = tempName.substring(0, tempName.lastIndexOf('.'));//remove .{number}
		    	
		    	if(!fileNames.contains(fileName))
		    		fileNames.add(fileName);
		    }
		}		
		return fileNames;
	}
	
    /*
     * Split/merger
     * http://stackoverflow.com/questions/10864317/how-to-break-a-file-into-pieces-using-java
     */
	
	//Split the file in chunks
    public static void splitFile(File f) throws IOException {
        int partCounter = 1;//I like to name parts from 001, 002, 003, ...
                            //you can change it to 0 if you want 000, 001, ...

        //int sizeOfChunks = 1024 * 1024;// 1MB
        int sizeOfChunks = (int) (Math.ceil(f.length() / (float) nrSenders));	//divide the file in number of chunks equal to number of senders (round up odd numbers)
       // int sizeOfChunksC = (int) (Math.ceil(f.length() / (float)nrSenders));
        //System.out.println("Real lenght: " + f.length() + "\n" +((float)(f.length() / (float)nrSenders)) + "\nand ceilling: " + sizeOfChunks);
        byte[] buffer = new byte[sizeOfChunks];

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f))) {//try-with-resources to ensure closing stream
            String name = f.getName();

            int tmp;
            while ((tmp = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                File newFile = new File(chunksFolder, name + "."
                        + String.format("%03d", partCounter++));
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, tmp);//tmp is chunk size
                }
            }
        }
    }
    
 
    //Merge Files
    public static void mergeFiles(List<File> files, File into)
            throws IOException {
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(
                new FileOutputStream(into))) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }
    
    //List the files that are split
    public static List<File> listOfFilesToMerge(File oneOfFiles) {
    	
    	System.out.println(oneOfFiles.getName() + " merging...");
    	
        String destFileName = oneOfFiles.getName();//{name}.{number}
        //String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));//remove .{number}
        	//^This code is used if all files are in the same folder^
        
        File[] files = oneOfFiles.getParentFile().listFiles(
                (File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
        Arrays.sort(files);//ensuring order 001, 002, ..., 010, ...
        return Arrays.asList(files);
    }
    
    public static void mergeFiles(File oneOfFiles, File into)
            throws IOException {
        mergeFiles(listOfFilesToMerge(oneOfFiles), into);
    }
    
    public static List<File> listOfFilesToMerge(String oneOfFiles) {
        return listOfFilesToMerge(new File(oneOfFiles));
    }

    public static void mergeFiles(String oneOfFiles, String into) throws IOException{
        mergeFiles(new File(oneOfFiles), new File(into));
    }
    /*
     * End split/merger
     */

}
