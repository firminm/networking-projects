# Multithreaded Barebones Social Network
Utilized the socket interface to send and recieve messages over the internet using TCP/IP to implement a simple social networking application.


### Functionality:
- Add friends with `@connect <username>`
  > See list of commands for friend-adding sequence
- Send status updates to friends using `#status <status>`
- Broadcasts global welcome message when a new user leaves/joins
- Broadcasts global friend update messages


To run on local with default port 58621
```sh
javac *.java
java Server 
```
or
```sh
javac *.java
java Server <port> <max number of users>
```

On local machine:
To run on local with default port 58621
```sh
java User
```
or
```sh
java User <addres> <port>
```

## List of commands
| Name                  | Syntax                   | Notes                                              | 
|-----------------------|--------------------------|----------------------------------------------------|
| Set Name              | `<name>`                 | when prompted on startup                           |
| Set Status            | `#status <status>`       |                                                    | 
| Send Friend Request   | `@connect <username>`    |                                                    |
| Accept Friend Request | `@friend <username>`     | Broadcasts "user1 and user2 are now friends"       |
| Deny Friend Request   | `@deny <username>`       |                                                    |
| Unfriend another user | `@disconnect <username>` | Broadcasts "user1 and user2 are no longer friends" |


## Protocol
| Action                                |    Client Message     |               Response to Client               |    Global Broadcast     | Friends Broadcast |
|---------------------------------------|:---------------------:|:----------------------------------------------:|:-----------------------:|------------------:|
| Set name (when prompted)              |    `<username>`       |                    #welcome                    | #\<username> has joined |                   |
| Set Status                            |  `#status <status>`   |                 #statusPosted                  | \<username>: \<status>  |                   |
| Send Friend Request                   | `@connect <username>` |        #FriendRequestDenied \<username>        |   #Leave \<username>    |                   |
| Accept Friend Request (when prompted) | `@friend <username>`  |   #OKfriends \<username1> \<username2><br/>    |                         |                   |
| Leave                                 |         `#Bye`        |                      #Bye                      |   #Leave \<username>    |                   |
