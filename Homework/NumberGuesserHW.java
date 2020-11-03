import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesserHW {
	private int level = 1;
	private int strikes = 0;
	private int maxStrikes = 5;
	private int number = 0;
	private int currentGuess = 0; //added variable to append to data file
	private boolean isRunning = false;
	final String saveFile = "numberGuesserSave.txt";

	/***
	 * Gets a random number between 1 and level.
	 * 
	 * @param level (level to use as upper bounds)
	 * @return number between bounds
	 */
	public static int getNumber(int level) {
		int rangeMult = new Random().nextInt(10) + 1; //new level generates random number in a range between 1-5 units bigger than the last level
		int range = 9 + ((level - 1) * rangeMult);
		System.out.println("I picked a random number between 1-" + (range + 1) + ", let's see if you can guess.");
		return new Random().nextInt(range) + 1;
	}

	private void win() {
		System.out.println("That's right!");
		level++;// level up!
		strikes = 0;
		System.out.println("Welcome to level " + level);
		number = getNumber(level);
		saveData();
	}

	private void lose() {
		System.out.println("Uh oh, looks like you need to get some more practice.");
		System.out.println("The correct number was " + number);
		strikes = 0;
		level--;
		if (level < 1) {
			level = 1;
		}
		number = getNumber(level);
		saveData();
	}

	private void processCommands(String message) {
		if (message.equalsIgnoreCase("quit")) {
			System.out.println("Tired of playing? No problem, see you next time.");
			isRunning = false;
		}
	}

	private void processGuess(int guess) {
		if (guess < 0) {
			return;
		}
		System.out.println("You guessed " + guess);
		if (guess == number) {
			win();
		} else {
			System.out.println("That's wrong");
			strikes++;
			if (strikes >= maxStrikes) {
				lose();
			} else {
				int remainder = maxStrikes - strikes;
				System.out.println("You have " + remainder + "/" + maxStrikes + " attempts remaining");
				if (guess > number) {
					System.out.println("Lower");
				} else if (guess < number) {
					System.out.println("Higher");
				}
			}
		}
		currentGuess = guess;
		saveData();
	}

	private int getGuess(String message) {
		int guess = -1;
		try {
			guess = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			System.out.println("You didn't enter a number, please try again");

		}
		return guess;
	}

	private void saveData() { //changed saveLevel method to saveData
		try (FileWriter fw = new FileWriter(saveFile)) {
			fw.write("" + level + " " + strikes + " " + number + " " + currentGuess);
		} catch (IOException e) {									// added 3 more variables to the file
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean loadData() { //changed loadLevel method to loadData
		File file = new File(saveFile);
		if (!file.exists()) {
			return false;
		}
		try (Scanner reader = new Scanner(file)) {
			while (reader.hasNextLine()) {
				int _level = reader.nextInt();
				int _strikes = reader.nextInt();
				int _number = reader.nextInt();
				int _guess = reader.nextInt(); //reads the added variables
				if (_level > 1 && _strikes > -1 && _number > 0 && _guess > 0) {
					strikes = _strikes;
					level = _level;
					number = _number;
					currentGuess = _guess;
					break;
				}
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e2) {
			e2.printStackTrace();
			return false;
		}
		return level > 1 && strikes > -1 && number > 0 && currentGuess > 0;
	}

	void run() {
		try (Scanner input = new Scanner(System.in);) {
			System.out.println("Welcome to Number Guesser 4.0!");
			System.out.println("I'll ask you to guess a number between a range, and you'll have " + maxStrikes
					+ " attempts to guess.");
			if (loadData()) {
				String hilo = "";
				if (currentGuess > number) {
					hilo = "Lower";
				} else if (currentGuess < number) {
					hilo = "Higher";
				}
				System.out.println("Successfully loaded last save");  //changed console outputs to match restored data
				System.out.println("Currently on level " + level + ";");
				System.out.println("You have used " + strikes + "/" + maxStrikes + " strikes");
				if (strikes > 0)
					System.out.println("Your last guess: " + currentGuess + " " + hilo);
				System.out.println("Let's continue then");
			}
			//number = getNumber(level);
			isRunning = true;
			while (input.hasNext()) {
				String message = input.nextLine();
				processCommands(message);
				if (!isRunning) {
					break;
				}
				int guess = getGuess(message);
				processGuess(guess);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		NumberGuesserHW guesser = new NumberGuesserHW();
		guesser.run();
	}
}
