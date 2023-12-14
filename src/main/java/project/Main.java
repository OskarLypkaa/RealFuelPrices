package project;


import project.exceptions.APIStatusException;
import project.exceptions.WSDataException;

public class Main {
    public static void main(String[] args) throws APIStatusException, InterruptedException, WSDataException {
        long startTime = System.currentTimeMillis();

        // Currently, I'm working on reviewing each individual class, therefore I'll leave this class empty ~ Oskar

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Time taken: " + duration + " milliseconds");
    }
}
