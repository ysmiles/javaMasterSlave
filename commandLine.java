package masterslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class commandLine {

    public String getCommand() throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(">");
        String s = bf.readLine();
        return s;
    }
}