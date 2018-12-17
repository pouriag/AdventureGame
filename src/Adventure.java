/*
 * File: Adventure.java
 * --------------------
 * This program plays the Adventure game from Assignment #4.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

/* Class: Adventure */
/**
 * This class is the main program class for the Adventure game.
 */

public class Adventure  {

	// the rooms in the game
	private SortedMap<Integer, AdvRoom> rooms = new TreeMap<Integer, AdvRoom>();
	// the list of the objects
	private HashMap<String, AdvObject> objects = new HashMap<String, AdvObject>();
	
	private HashMap<String,String> synonyms = new HashMap<String,String>();
	
	// the inventory of the player
	private static ArrayList<AdvObject> inventory;

	private static ArrayList<AdvMotionTableEntry> movements;
	
	private static AdvRoom room;
	
	private static int nextRoom = 1;

	// Use this scanner for any console input
	private static Scanner scan = new Scanner(System.in);
	private static Adventure game = new Adventure();

	private static boolean play = true;
	
	
	/**
	 * This method is used only to test the program
	 */
	public static void setScanner(Scanner theScanner) {
		scan = theScanner;
	}

	/**
	 * Runs the adventure program
	 */
	public static void main(String[] args) {
		System.out.print("What will be your adventure today? ");
		String name = scan.nextLine(); // small, crowther, tiny????
		init();
		try {
			game = createGame(name);
			System.out.println("Welcome to the "+name.toUpperCase()+" Adventure!\n");
			
			while(play) {
				room = game.rooms.get(nextRoom);
				movements = new ArrayList <AdvMotionTableEntry>();
				for (int i= 0 ; i< room.getMotionTable().length; i++){
					
					movements.add(room.getMotionTable()[i]);
				}

				game.executeDescribeRoom();
				
				//prints the prompt, takes the user input and converts it to uppercase
				//synonyms are not implemented as of yet
				System.out.print(">");
				String userInput = scan.nextLine().toUpperCase();

				String verb = "";
				String usedObject = "";
				
				
				
				
				if (userInput.contains(" ")) {
					String[] strings = new String[2];
					strings = userInput.split("\\s+");
					for (int i  =0 ; i < strings.length; i++){
						if (game.synonyms.containsKey(strings[i])){
							strings[i] = game.synonyms.get(strings[i]) ;
						}
					}
					verb = strings[0];
					if (strings.length != 1){
						usedObject = strings[1];
					}
					
					
				}
				else {
					if (game.synonyms.containsKey(userInput)){
						verb = game.synonyms.get(userInput);
					}else{
					verb = userInput;
					}
				}
				if (verb.equals("HELP")){
					game.executeHelpCommand();
					continue;
				}
				
				if (verb.equals("LOOK")){
					game.executeLookCommand();
					continue;
				}
				if (verb.equals("TAKE")){
					AdvObject obj = game.objects.get(usedObject);
					game.executeTakeCommand(obj);
					continue;
				}
				if (verb.equals("DROP") || verb.equals("RELEASE")){
					AdvObject obj = game.objects.get(usedObject);
					game.executeDropCommand(obj);
					continue;
				}
				if(verb.equals("INVENTORY")){
					game.executeInventoryCommand();
					continue;
				}
				if (verb.equals("QUIT")){
					game.executeQuitCommand();
				}
				else {
					game.executeMotionCommand(verb);
				}
				
			}
		    
			
		} catch (IOException e) {
			System.out.println("Could not find the files for " + name);
		}
	}

	
	private static void init() {
		room = null;
		nextRoom= 1;
		play =true;
		inventory = new ArrayList<AdvObject>();
	}

	/*Method createGame(String name)*/
	/*
	 * captures a string and load all the contents of the text files
	 * in the designed data structure. and send back the game.
	 */
	public static Adventure createGame(String name) throws IOException {
		
		
		// read the rooms
		Scanner scan = new Scanner(new File(name + "Rooms.txt"));
		while (scan.hasNextLine()) {
			AdvRoom room = AdvRoom.readFromFile(scan);
			game.rooms.put(room.getRoomNumber(), room);
			
		}
		scan.close();
		
		if (!name.equals("tiny") ){
			// read the objects
			scan = new Scanner(new File(name + "Objects.txt"));
			
			while (scan.hasNextLine()) {
				
				// Be careful with blank lines
				AdvObject object = AdvObject.readFromFile(scan);
				game.objects.put(object.getName(),object);
				
				// place the object in its corresponding room
				room = game.rooms.get(object.getInitialLocation());
				room.addObject(object);
			}
			scan.close();
			// read the synonyms
		
			scan = new Scanner(new File(name + "Synonyms.txt"));
			while(scan.hasNext()){
				String line = scan.nextLine();
				String[] parts = line.split("=");
				game.synonyms.put(parts [0], parts [1]);
			}
		
			scan.close();

		
		}
		return game;
		
	}
	
	
	/* Method: executeMotionCommand(direction) */
	/**
	 * Executes a motion command. This method is called from the
	 * AdvMotionCommand class to move to a new room.
	 * 
	 * @param direction
	 *            The string indicating the direction of motion
	 */
	public void executeMotionCommand(String direction) {
		
		// Replace with your code
		//initializing the values that we need
		int index = 0;
		AdvRoom Room = new AdvRoom ();
		int destRoom = 0;
		boolean found = false;
		String keyName = " ";
		
		
		for (int i = 0 ; i < room.getMotionTable().length; i++){
			
			//check to see if it's possible to go toward that direction 
			if (room.getMotionTable()[i].getDirection().equals(direction)){
				index = i;
				found = true;
				keyName = room.getMotionTable()[i].getKeyName();
				destRoom = room.getMotionTable()[i].getDestinationRoom();
				break;
			}	
		}
		//if possible
		if(found){
			//check to see if it's unlocked
			if(unlocked(keyName)){
				//check to make sure it won't lead to a forced room
				if (!forced(destRoom)){
					nextRoom = destRoom;	
				}
				//unlocked forced room
				else {
					Room =  rooms.get(destRoom);
				
					//check to see if player dies
					if (Room.getMotionTable()[0].getDestinationRoom() == 0 && Room.getRoomNumber() != 77){

						for (String line :Room.getDescription()){
							System.out.println(line);
						}
						System.out.println("You Died");
						System.out.println("Game Over");
						play = false;
						return;
						
					//check to see if player wins	
					}else if (Room.getMotionTable()[0].getDestinationRoom() == 0 && Room.getRoomNumber() == 77 ){

						for (String line :Room.getDescription()){
							System.out.println(line);
						}
						play = false;
						return;
					}
					
					//alive and continuing the game
					else{

						for (int i = 0 ;i < Room.getMotionTable().length;i++ ){
							keyName = Room.getMotionTable()[i].getKeyName();
							
							//if the inside forced room is unlocked and available
							if (unlocked(keyName)){

								nextRoom = Room.getMotionTable()[i].getDestinationRoom();
								if (forced(nextRoom)){

									room = rooms.get(nextRoom);
									for (String line : room.getDescription()){
										System.out.println(line);
									}
									
									//used recursion method for nested forced rooms 
									executeMotionCommand("FORCED");
									break;
								}
								else{
									for (String line : Room.getDescription()){
										System.out.println(line);
									}
								}
							}
						}
					}
				}
			}
			else {
				while(!unlocked(keyName)){
					index ++;
					destRoom = room.getMotionTable()[index].getDestinationRoom();
					keyName = room.getMotionTable()[index].getKeyName();
				}
				if(!forced(destRoom)){

					nextRoom = destRoom;
					room = rooms.get(nextRoom);
					
				}else {

					System.out.println(11);
					room = rooms.get(destRoom);
					
					for (String line : room.getDescription()){
						System.out.println(line);
					}
					executeMotionCommand("FORCED");

				}
			}
		}
		//if couldn't find direction
		else{
			System.out.println("unfortunately there is no way that you can do that and"
					+ " you are making a fool out of yourself for trying.");
		}
				
	}
	
	
	/*Method Unlocked (String keyname)*/
	/**
	 * check to see if the player has the required item 
	 * in the inventory.
	 * 
	 */
	
	public boolean unlocked(String keyname) {
		if (keyname == null) {
			return true;
		}
		else if( keyname != null) { 
			if (!inventory.contains(objects.get(keyname)))
			return false;
				
			}
		return true;
	}
	
	
	/*Method forced()*/
	/**
	 * a method that checks if the next room is forced or not?
	 *  returns a boolean
	 */
	public boolean forced(int entRoom){
		AdvMotionTableEntry[] motionTable = rooms.get(entRoom).getMotionTable();
		for(int i = 0; i < motionTable.length; i++){
			if(motionTable[i].getDirection().equals("FORCED")) return true;
		}
		return false;
	}
		

	/* Method: executeQuitCommand() */
	/**
	 * Implements the QUIT command. This command should ask the user to confirm
	 * the quit request and, if so, should exit from the play method. If not,
	 * the program should continue as usual.
	 */
	public void executeQuitCommand() {
		// Replace with your code
		while(true){	
			System.out.println("Are you sure you want to quit now? ");
			
			String response = scan.nextLine();
			response = response.toLowerCase();
			
			if(response.equals("yes") ||response.equals("y") ) {
				play = false;
				break;
			}
			else if(response.equals("no")||response.equals("n")) break;
			else System.out.println("Please answer with yes or no.");
		}
	}

	/* Method: executeHelpCommand() */
	/**
	 * Implements the HELP command. Your code must include some help text for
	 * the user.
	 */
	public void executeHelpCommand() {

		 // Replace with your code
		AdvMotionTableEntry[] possibleMotions = room.getMotionTable();
		System.out.println("You can type in :");
		for (int i =0; i < possibleMotions.length;i++){
			System.out.print(possibleMotions[i].getDirection()+ ", ");
			
		}
		System.out.println();
	}
	/*Method executeDescribeRoom()*/
	/**
	 * describes what's inside room then set @hasBeenVisited() to true;
	 * 
	 */
	public void executeDescribeRoom() {

		if(!(room.hasBeenVisited())){
			String[] description = room.getDescription();
			for(String line: description){
				System.out.println(line);
			}
			
		
			for(AdvObject obj: room.getAllObjects()){
				System.out.println("There is " + obj.getDescription() + " here.");
			}
			room.setVisited(true);
		}
		else{
			System.out.println(">"+room.getName());
		}
	}

	/* Method: executeLookCommand() */
	/**
	 * Implements the LOOK command. This method should give the full description
	 * of the room and its contents.
	 */
	public void executeLookCommand() {
		 // Replace with your code

		room.setVisited(false);

		executeDescribeRoom();
		System.out.println("");
		room.setVisited(true);

	}

	/* Method: executeInventoryCommand() */
	/**
	 * Implements the INVENTORY command. This method should display a list of
	 * what the user is carrying.
	 */
	public void executeInventoryCommand() {
		 // Replace with your code
		if(!inventory.isEmpty()){
			System.out.println("You are carrying: ");
			for(int i = 0; i < inventory.size(); i++){
				System.out.println(inventory.get(i).getName()+": "+ inventory.get(i).getDescription());
			}
		}else System.out.println("you don't have anything in your inventory");
	}

	/* Method: executeTakeCommand(obj) */
	/**
	 * Implements the TAKE command. This method should check that the object is
	 * in the room and deliver a suitable message if not.
	 * 
	 * @param obj
	 *            The AdvObject you want to take
	 */
	public void executeTakeCommand(AdvObject obj) {
		// Replace with your code
		if(!(room.containsObject(obj))){
			System.out.println("There's no such thing here.");
		} else {
				room.removeObject(obj);
				inventory.add(obj);
				System.out.println("Taken.");
			
		} 
	}

	/* Method: executeDropCommand(obj) */
	/**
	 * Implements the DROP command. This method should check that the user is
	 * carrying the object and deliver a suitable message if not.
	 * 
	 * @param obj
	 *            The AdvObject you want to drop
	 */
	public void executeDropCommand(AdvObject obj) {
		// Replace with your code
		if(!(inventory.contains(obj))){
			System.out.println("You don't have that in your inventory.");
		} else {
			
		
			room.addObject(obj);
			inventory.remove(obj);
			System.out.println("Dropped.");
			}
		}
	

	/* Private instance variables */
	// Add your own instance variables here

}


