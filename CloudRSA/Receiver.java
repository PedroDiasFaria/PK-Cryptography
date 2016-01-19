import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Pedro Faria on 19-Nov-15.
 */
public class Receiver {
	public Receiver() {
		// TODO Auto-generated constructor stub
	}

	public String receiveFile(List<String> fileList){

		System.out.println("Which file you want to receive?:");

		for(int i = 0 ; i < fileList.size(); i++){
			System.out.println("(" + i + "): " + fileList.get(i));
		}
		int fileToReceive = -1;

		Scanner sc = new Scanner(System.in);

		while (fileToReceive >= fileList.size() || fileToReceive < 0 ){
			
				System.out.println("(Input the number of the file)");
				try{
					fileToReceive = sc.nextInt();
					System.out.println(fileToReceive);
				}catch(InputMismatchException ex){
					System.out.println("Wrong input. Ony integer input will be processed.");
					//clean up the garbage input
					sc.next();
				}
		}

		sc.close();

		System.out.println("Selected: " + fileList.get(fileToReceive));


		return fileList.get(fileToReceive);

	}
}
