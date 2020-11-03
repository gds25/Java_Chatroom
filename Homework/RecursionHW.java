import java.util.InputMismatchException;
import java.util.Scanner;
public class RecursionHW {
		  
	public static void main(String[] args) {	
		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("Enter a number: ");
			int forNum = scan.nextInt();
			int forSum = 0;
			
			int whileNum = forNum;
			int whileSum = 0;
			
			//get sum using while loop
			while (whileNum > 0) {
				whileSum += whileNum;
				whileNum--;
			}
			System.out.println("Sum using while loop: " + whileSum);
			
			//get sum using for loop
			for (int i = forNum; i > 0; i--) {
				forSum += i;
			}
			System.out.println("Sum using for loop: " + forSum);
		}
		catch (InputMismatchException e) {
			System.out.println("Please enter an integer.");
			e.printStackTrace();
		}
		
	}
}
