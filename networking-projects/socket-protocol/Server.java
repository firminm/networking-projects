//package pa1part3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;


/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket userSocket = null;
	// Maximum number of users 
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58621;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
			System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);
		}


		
		userThread[] threads = new userThread[maxUsersCount];


		/*
		 * Open a server socket on the portNumber (default 8000). 
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;

	// Added in pt3, used for friending
	private Set<String> friends;
	private Set<String> pendingFriends;		// INCOMING friend requests

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
		this.friends = new HashSet<>();
		this.pendingFriends = new HashSet<>();	// INCOMING
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;

		try {
			/* Create input and output streams for this client. */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());

			/* Read username & welcome user */
			processInput(input_stream.readLine());

//			/* Welcome the new user. */
//			sendMessage("#welcome");
//			broadcastMessage("#newuser " + userName);


			/* Start the conversation. */
			boolean running = true;
			while (running) {
				try{
					String message = input_stream.readLine();
					System.out.println(message);
					running = processInput(message);
				}
				catch (java.net.SocketException e){
					System.out.println("Connection with " + userName + " reset, terminating connection");
					running = false;
				}
				catch (Exception e){
					System.out.println(e);
					sendMessage("Error 500");
				}

			}
			System.out.println("exited");

			// Server has recieved #bye message

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		} catch (IOException e) {
		}
	}

	/**
	 *
	 * @param line 	User input
	 * @return		Client's desire to continue
	 */
	private boolean processInput(String line){
		if (line.startsWith("#status ")){
			// Changed in Pt3
			String status = line.substring(7).trim();
			notifyFriends("#" + userName + ": " + status);

			sendMessage("#statusPosted");
		}
		else if (line.startsWith("#friendme ")){
			// Client receives command "@connect"
			// Send friend request, allows repetitions
			String user = line.substring(10).trim();

			if (friends.contains(user)){
				sendMessage("You are already friends with " + user);
			}
			else{

				sendFriendReq(user, "#friendme " + this.userName);
			}
		}
		else if (line.startsWith("#friends ")){
			// user has accepted a friend request
			String user = line.substring(9).trim();

			// Check if specified user has first sent a friend request to the client
			if (pendingFriends.contains(user)){
				makeFriends(user);
			}
			else{
				sendMessage(user + " has not sent you a friend request");
			}
		}
		else if (line.startsWith("#DenyFriendRequest ")){
			String user = line.substring(19).trim();

			if (pendingFriends.contains(user)) {
				denyFriendReq(user);
			}
			else{
				sendMessage(user + " has not sent you a friend request");
			}
		}
		else if (line.startsWith("#unfriend ")){
			String user = line.substring(10).trim();

			if (this.friends.contains(user)){
				unfriend(user);
			} else{
				sendMessage(user + " is not on your friends list");
			}
		}
		/* End of Pt 3 code */

		else if (line.startsWith("#Bye")) {
			sendMessage("#Bye");
			broadcastMessage("#Leave " + userName);
			// Socket closing occurs in run() thread
			return false;
		}
		else if (line.startsWith("#join ")){
			userName = line.substring(5).trim();
			sendMessage("#welcome");
			broadcastMessage("#newuser " + userName);

			initFriends();
		}
		else{
			sendMessage("Invalid input: " + line);
		}

		return true;
	}

	/**
	 * Broadcasts a message to ALL clients
	 * Calls individual thread's sendMessage() function - SYNCHRONIZED
	 * @param message	message to send to all users
	 */
	private synchronized void broadcastMessage(String message){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i] != this) {
				threads[i].sendMessage(message);
			}
		}
	}

	/**
	 * Sends message to connected client
	 * @param message	message to be sent to client
	 */
	public void sendMessage(String message){
		String output = message;
//		output_stream.writeBytes(output.getBytes(StandardCharsets.UTF_8));
		output_stream.println(output);
		output_stream.flush();
	}


	/* Added in Pt 3 */

	/**
	 * Broadcasts a message to FRIENDS ONLY
	 * Friends are identified by userNames
	 * Calls friendly threads sendMessage() function
	 */
	private synchronized void notifyFriends(String message){
		// Loop through all threads, only send message to the threads whose usernames are marked as a friend
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i] != this && this.friends.contains(threads[i].userName)) {
				threads[i].sendMessage(message);
			}
		}
	}

	/**
	 * Makes two clients friends
	 * @param user userName of the original friender
	 */
	private synchronized void makeFriends(String user){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i].userName.equals(user)) {
				this.friends.add(user);
				threads[i].friends.add(this.userName);
				pendingFriends.remove(user);

				// Send confirmation to both parties
				String confirmation = "#OKFriends " + user + " " + userName;
				threads[i].sendMessage(confirmation);
				sendMessage(confirmation);

				// Broadcast Message to all clients
				broadcastMessage(user + " and " + userName + " are now friends");

				return;
			}
		}
	}

	/**
	 * Denies friend request
	 * @param user userName of the original friender
	 */
	private synchronized void denyFriendReq(String user){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i].userName.equals(user)) {
				threads[i].sendMessage("#FriendRequestDenied " + this.userName);

				pendingFriends.remove(user);
				return;
			}
		}
	}

	/**
	 * Removes a friend from friends list
	 */
	private synchronized void unfriend(String user){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i].userName.equals(user)) {
				threads[i].friends.remove(this.userName);
				this.friends.remove(user);

				broadcastMessage(user + " " + userName + " are no longer friends");
				threads[i].sendMessage("#NotFriends " + user + " " + userName);
			}
		}
	}


	/**
	 * Sends a message to a user via their userName
	 * @return user was found ? true : false
	 */
	private synchronized boolean sendFriendReq(String user, String message){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i] != this && threads[i].userName.equals(user)) {
				threads[i].pendingFriends.add(this.userName);
				threads[i].sendMessage(message);
				return true;
			}
		}
		return false;
	}

	/**
	 * Attempts to reestablish friendships of users which have stayed online since this one has disconnected
	 * Called on #join
	 *
	 * I am aware that I could make a static hashmap of username->[friends] but I am too deep at this point
	 */
	private synchronized void initFriends(){
		for (int i = 0; i < maxUsersCount; i++) {
			if (threads[i] != null && threads[i] != this) {
				if (threads[i].friends.contains(this.userName)){
					this.friends.add(threads[i].userName);
				}
			}
		}
	}
}




