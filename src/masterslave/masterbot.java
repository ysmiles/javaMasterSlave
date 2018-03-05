package masterslave;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.IOException;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;

public class masterbot extends Thread {

    // This list is used for storing slaves information.
    static List<slave> slaves = new ArrayList<slave>();

    // Specify date format
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String argv[]) throws Exception {
        // basic setting
        if (argv.length != 2 || (!argv[0].equals("-p"))) {
            System.out.println("Correct way: masterbot -p listenport");
            System.exit(1);
        }
        int listenport = Integer.parseInt(argv[1]);

        ServerSocket ssock = new ServerSocket(listenport);
        System.out.println("Bind to " + listenport + " successful. Now listening.");

        // start run() to get command
        (new masterbot()).start();

        while (true) {
            Socket sock = ssock.accept();
            System.out.println("\nSlave connected");

            // After connection, add a slave information to record.
            String dateString = dateFormat.format(new Date());
            addElement(new slave(sock, dateString));

            // Create a receiver
            // 1. receive message from slave
            // 2. check connection
            new Thread(new msgReceiver(sock)).start();

            System.out.println("Slave added into list");
            System.out.print(">");
        }

    }

    public void run() {
        try {
            while (true) {
                // get new command
                String com = (new commandLine()).getCommand();
                // create a parser object
                commandParser par = new commandParser(com);

                // handle command
                if (par.getCommandType() == 1) {
                    // list slave
                    listSlaves();
                }
                // parsing complex command
                else {
                    // check if it is valid
                    if (par.isValid()) {
                        // send to all
                        if (par.getSlaveIdentifier().equals("all")) {
                            for (int i = 0; i < getSize(); i++) {
                                Socket thesocket = getElementSock(i);
                                new Thread(new commandSender(thesocket, com)).start();
                                System.out.println("Master has successfully send command to slave " + i + ".");
                            }
                        }
                        // send to a specific slave
                        else {
                            int index = getElementIndex(par.getSlaveIdentifier(), par.getSlaveIdentifierType(),
                                    par.getSlaveport());
                            if (index != -1) {
                                Socket sock = getElementSock(index);
                                new Thread(new commandSender(sock, com)).start();
                                System.out.println("Master has successfully send command to slave " + index + ".");
                            } else {
                                System.out.println("Connot find the specific slave: " + par.getSlaveIdentifier());
                                if (par.getSlaveport() != -1)
                                    System.out.println(" with the portnumber " + par.getSlaveport());
                            }
                        }
                    }
                    // invalid command
                    else
                        par.correctUse();

                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    // List all slaves information with this master server
    public static void listSlaves() {
        synchronized (slaves) {
            System.out.println("SlaveName IPAddress Port RegistrationDate");
            for (slave i : slaves) {
                System.out.println(i.getName() + " " + i.getIPaddr() + " " + i.getSourcePortNumber() + " "
                        + i.getRegistrationDate());
            }
        }
    }

    // synchronized add
    public static void addElement(slave s) {
        synchronized (slaves) {
            slaves.add(s);
        }
    }

    // Find the first match
    public static int getElementIndex(String identifier, String identifierType, int port) {
        synchronized (slaves) {
            // return the fist found one, else return -1
            if (identifierType.equals("IP")) {
                for (int i = 0; i < slaves.size(); i++) {
                    if (slaves.get(i).getIPaddr().equals(identifier)) {
                        if (port == -1)
                            return i;
                        else if (slaves.get(i).getSourcePortNumber() == port)
                            return i;
                    }
                }
            } else {
                for (int i = 0; i < slaves.size(); i++) {
                    if (slaves.get(i).getName().equals(identifier)) {
                        if (port == -1)
                            return i;
                        else if (slaves.get(i).getSourcePortNumber() == port)
                            return i;
                    }
                }
            }
            return -1;
        }
    }

    // synchronized get
    public static Socket getElementSock(int index) {
        synchronized (slaves) {
            return slaves.get(index).getSock();
        }
    }

    // synchronized size()
    public static int getSize() {
        synchronized (slaves) {
            return slaves.size();
        }
    }

    // synchronized remove by index
    public static boolean remover(int index) {
        synchronized (slaves) {
            try {
                slaves.remove(index);
                return true; // successfully removed
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }
    }

    // synchronized remove by socket
    public static boolean removeBySocket(Socket s) {
        synchronized (slaves) {
            for (int i = 0; i < slaves.size(); i++) {
                if (slaves.get(i).getSock().equals(s)) {
                    System.out.println("Removing: ");
                    System.out.println(slaves.get(i).getName() + " " + slaves.get(i).getIPaddr() + " "
                            + slaves.get(i).getSourcePortNumber() + " " + slaves.get(i).getRegistrationDate());
                    slaves.remove(i);
                    return true;
                }
            }
            return false;
        }
    }

}
