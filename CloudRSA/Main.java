import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Main {

	public static String filesFolder = "FilesAvailable/";
	public static String chunksFolder = "Chunks/";
	public static String cloudFolder = "Cloud/";
	public static String receiverFolder = "Receiver/";
    public static String fileName = "HSsc.png";//"testFile.txt";
    
    public static int nrSenders = 2;
    
    static RSA rsa = null;
    static Receiver receiver = null;
    static List<Sender> senderVec = null;
    static Cloud cloud = null;
    
    //TODO each sender sends the chunk with his id 
    public static void main(String[] args) throws IOException{

    	initializeSystem();
    	
    	testFileSplitter();
        //System.out.println(rsa.getKey_par_gen().toString());
        System.out.println(fileName);
    }

    private static void testFileSplitter() throws IOException{
    	splitFile(new File(filesFolder + fileName));
    	    	
    	List<File> lf = listOfFilesToMerge(new File(chunksFolder + fileName));
    	
    	if(!lf.isEmpty())
    		System.out.println(lf.toString());
    	
    	String testReceiverFileName = receiver.receiveFile(getCloudFiles());
    	
    	mergeFiles(lf, new File(receiverFolder + testReceiverFileName));
    }
    
	private static void initializeSystem() {
	    //rsa = new RSA();
	    receiver = new Receiver();
	    senderVec = new Vector<Sender>();

	    for(int i = 0; i < nrSenders; i++){
	    	senderVec.add(new Sender(i));
	    }
	    
	    cloud = new Cloud();
	}
	
	//TODO put in the cloud class instead of main
	//Content of the cloud
	public static List<String> getCloudFiles(){		
		
		List<String> fileNames = new ArrayList<String>();


		File[] files = new File(chunksFolder).listFiles();
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
        int sizeOfChunksC = (int) (Math.ceil(f.length() / (float)nrSenders));
        System.out.println("Real lenght: " + f.length() + "\n" +((float)(f.length() / (float)nrSenders)) + "\nand ceilling: " + sizeOfChunksC);
        byte[] buffer = new byte[sizeOfChunks];

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f))) {//try-with-resources to ensure closing stream
            String name = f.getName();

            int tmp = 0;
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
    	
    	System.out.println(oneOfFiles.getName());
    	
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
