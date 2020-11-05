package com.searise.sof;

import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

public class CliDriver {

    public static void main(String[] args) {
        new CliDriver().run();
    }

    private Driver driver = new Driver();

    private CliDriver() {
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        while (true) {
            String cmd = waitForCmd(in);
            if (StringUtils.equalsAnyIgnoreCase("exit", cmd)) {
                driver.stop();
                return;
            }

            try {
                driver.compile(cmd);
            } catch (Exception e) {
                driver.stop();
                System.out.println(e.getMessage());
            }
        }
    }

    private static final String headPrefix = "sof> ";
    private static final String nextPrefix = "   > ";
    private static final String EOF = ";";
    private static final StringBuilder cmdBuilder = new StringBuilder();

    private String waitForCmd(Scanner in) {
        print(headPrefix);
        String nextLine = in.nextLine();
        if (StringUtils.lastIndexOf(nextLine, EOF) >= 0) {
            return nextLine.substring(0, nextLine.length() - 1);
        }

        cmdBuilder.setLength(0);
        cmdBuilder.append(nextLine);
        while (true) {
            print(nextPrefix);
            nextLine = in.nextLine();

            if (StringUtils.lastIndexOf(nextLine, EOF) >= 0) {
                cmdBuilder.append(nextLine, 0, nextLine.length() - 1);
                return cmdBuilder.toString();
            }
            cmdBuilder.append(nextLine);
        }
    }

    private void print(String str) {
        System.out.print(str);
    }
}
