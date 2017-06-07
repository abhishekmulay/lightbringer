package hw2;

import java.io.File;
import java.io.PrintStream;

/**
 * Created by Abhishek Mulay on 6/7/17.
 */
public class App {

    private static void index() {

    }

    private static void merge() {

    }

    public static void main(String[] args) {
        // Writes output to output.txt file instead of stdout
        try {
            System.setOut(new PrintStream(new File("output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        long timeAtStart = System.nanoTime();
        ///////////////////////////////////////////////////////////////////
            index();
            merge();
        ///////////////////////////////////////////////////////////////////
        long timeAtEnd = System.nanoTime();
        long elapsedTime = timeAtEnd - timeAtStart;
        double seconds = (double) elapsedTime / 1000000000.0;
        System.out.println("Total time taken: " + seconds / 60.0 + " minutes");
    }
}
