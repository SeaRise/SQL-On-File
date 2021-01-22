package com.searise.sof.cli;

import com.searise.sof.core.SofSession;
import com.searise.sof.core.Utils;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

public class CliDriver {

    public static void main(String[] args) {
        new CliDriver().run();
    }

    private final SofSession session;

    private CliDriver() {
        session = SofSession.builder().build();
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        while (true) {
            String cmd = waitForCmd(in);
            if (StringUtils.equalsAnyIgnoreCase("exit", cmd)) {
                session.close();
                return;
            }

            try {
                session.compile(cmd);
            } catch (Exception e) {
                session.close();
                Utils.println(e.getMessage());
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
