//package pa1part3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class User extends Thread {
	private static String name;

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 58621;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.parseInt(args[1]);
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social network
				System.out.println("Please enter your name: ");
				name = inputLine.readLine().trim();
				String requestToJoin = "#join " + name;
//				output_stream.writeBytes(requestToJoin.getBytes(StandardCharsets.UTF_8));
				output_stream.println(requestToJoin);
				output_stream.flush();
				// Reading of response to message occures in worker thread

				String message;
				while (!closed) {
					String userInput = inputLine.readLine().trim();

					String userName;	// not used outside of if/else (I just don't feel like rewriting it)
					if (userInput.startsWith("@connect ")){
						userName = userInput.substring(9).trim();
						message = "#friendme " + userName;

					}
					else if (userInput.startsWith("@friend ")){
						userName = userInput.substring(8).trim();
						message = "#friends " + userName;
					}
					else if (userInput.startsWith("@deny ")){
						userName = userInput.substring(6).trim();
						message = "#DenyFriendRequest " + userName;
					}
					else if (userInput.startsWith("@disconnect ")){
						userName = userInput.substring(12).trim();
						message = "#unfriend " + userName;
					}


					/* end pt3 code */

					else{
						// Normal case (part 1 & 2 encapsulated)
						message = userInput;
					}


//					message += "\n";

					// Read user input and send protocol message to server
//					output_stream.writeBytes(message.getBytes(StandardCharsets.UTF_8));
					output_stream.println(message);
					output_stream.flush();

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		
		try {
			while ((responseLine = input_stream.readLine()) != null) {
//				responseLine = responseLine.trim();
				// Display on console based on what protocol message we get from server.
				if (responseLine.equals("#Bye") || responseLine.equals("#busy")) {
					System.out.println(responseLine.trim());
					break;
				}

				/* Begin code for pt3 */
				else if (responseLine.startsWith("#friendme ")){
					String user = responseLine.substring(10).trim();
					System.out.println(user + ": has sent you a friend request");
				}
				else if (responseLine.startsWith("#FriendRequestDenied ")){
					String user = responseLine.substring(21).trim();
					System.out.println(user + " rejected your friend request");
				}
				else if (responseLine.startsWith("#statusPosted")){
					; // I don't like it when this one appears honestly
				}
				else if (responseLine.startsWith("#NotFriends ")){
					String[] suffix = responseLine.substring(12).trim().split(" ");
					for (String user : suffix){
						if (!user.equals(name)){
							System.out.println(user + " has unfriended you");
							break;
						}
					}
				}
				else{
					// Server responses that don't need special handling (#welcome, #statusPosted, ...)
					System.out.println(responseLine);
				}
			}
			System.out.println("Hope to see you again !");
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}



