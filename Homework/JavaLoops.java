import java.util.Arrays;
import java.util.Scanner;

public class JavaLoops {

	public static void main(String[] args) {
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter size of array (greater than one): ");
		int size = scan.nextInt();
		int[] numbers = new int[size]; //creates the array of given size
		
		int nextNum;
		//loop fills array with integers input into the console
		for (int i = 0; i < size; i++) {
			if (i == 0) 
				System.out.print("Enter first number in array: ");
			else 
				System.out.print("Enter next number in array: ");
			nextNum = scan.nextInt();
			numbers[i] = nextNum;	
		}
		
		Arrays.sort(numbers); //sorts the array in numerical order
		System.out.print("Array in numerical order: " + Arrays.toString(numbers)); //prints sorted array
		
		System.out.print("\nEven numbers in array: ");
		//loop goes over all numbers in array
		for (int num : numbers) {
			if (num % 2 == 0) //this divides the given number by 2, if remainder is 0 then number is even
				System.out.print(num + " "); //prints even numbers
		}
		
	}
}
