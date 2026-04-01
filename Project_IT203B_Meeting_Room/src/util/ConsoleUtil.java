package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ConsoleUtil {
    private static final Scanner scanner = new Scanner(System.in, "UTF-8");
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void success(String msg) { System.out.println("[OK] " + msg); }
    public static void error(String msg)   { System.out.println("[LOI] " + msg); }
    public static void warn(String msg)    { System.out.println("[CANH BAO] " + msg); }
    public static void info(String msg)    { System.out.println("[INFO] " + msg); }

    public static void printHeader(String title) {
        System.out.println("========================================");
        System.out.println("  " + title);
        System.out.println("========================================");
    }

    public static void printSeparator() {
        System.out.println("----------------------------------------");
    }

    public static String inputString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            error("Khong duoc de trong, vui long nhap lai.");
        }
    }

    public static String inputOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int inputInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v > 0) return v;
                error("Vui long nhap so nguyen duong.");
            } catch (NumberFormatException e) {
                error("Sai dinh dang so, vui long nhap lai.");
            }
        }
    }

    public static double inputDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v >= 0) return v;
                error("Vui long nhap so khong am.");
            } catch (NumberFormatException e) {
                error("Sai dinh dang so, vui long nhap lai.");
            }
        }
    }

    public static LocalDateTime inputDateTime(String prompt) {
        while (true) {
            System.out.print(prompt + " (dd/MM/yyyy HH:mm): ");
            try {
                return LocalDateTime.parse(scanner.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                error("Dinh dang sai. Vi du: 25/06/2025 09:00");
            }
        }
    }

    public static String inputPassword(String prompt) {
        System.out.print(prompt);
        java.io.Console cons = System.console();
        if (cons != null) {
            char[] pwd = cons.readPassword();
            return pwd == null ? "" : new String(pwd);
        }
        return scanner.nextLine();
    }

    public static boolean confirm(String prompt) {
        System.out.print(prompt + " (y/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }

    public static int readChoice(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void pause() {
        System.out.print("\nNhan Enter de tiep tuc...");
        scanner.nextLine();
    }
}
