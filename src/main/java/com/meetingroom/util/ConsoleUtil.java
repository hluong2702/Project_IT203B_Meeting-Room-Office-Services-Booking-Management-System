package com.meetingroom.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Tiện ích giao diện console:
 *  - Màu ANSI cho tiêu đề, lỗi, thành công
 *  - In bảng đẹp bằng ký tự ASCII
 *  - Nhập liệu có kiểm tra kiểu dữ liệu
 */
public class ConsoleUtil {

    // ──────────────── MÀU ANSI ────────────────
    public static final String RESET   = "\u001B[0m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String CYAN    = "\u001B[36m";
    public static final String BOLD    = "\u001B[1m";

    private static final Scanner scanner = new Scanner(System.in, "UTF-8");
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ──────────────── THÔNG BÁO ────────────────
    public static void success(String msg) { System.out.println(GREEN + "✔ " + msg + RESET); }
    public static void error(String msg)   { System.out.println(RED   + "✘ " + msg + RESET); }
    public static void warn(String msg)    { System.out.println(YELLOW + "⚠ " + msg + RESET); }
    public static void info(String msg)    { System.out.println(CYAN  + "ℹ " + msg + RESET); }

    /** In tiêu đề menu đóng khung. */
    public static void printHeader(String title) {
        int len = 54;
        String border = "═".repeat(len);
        System.out.println(BOLD + BLUE + "╔" + border + "╗");
        System.out.printf("║  %-" + (len - 2) + "s  ║%n", title);
        System.out.println("╚" + border + "╝" + RESET);
    }

    /** In đường kẻ ngang phân cách. */
    public static void printSeparator() {
        System.out.println(BLUE + "─".repeat(56) + RESET);
    }

    /**
     * In bảng dữ liệu.
     * @param headers  Tiêu đề cột
     * @param widths   Độ rộng từng cột
     * @param rows     Dữ liệu (mỗi phần tử là một hàng)
     */
    public static void printTable(String[] headers, int[] widths, String[][] rows) {
        StringBuilder top = new StringBuilder("+");
        for (int w : widths) top.append("-".repeat(w + 2)).append("+");
        System.out.println(CYAN + top + RESET);

        StringBuilder hRow = new StringBuilder(BOLD + "|");
        for (int i = 0; i < headers.length; i++) {
            hRow.append(String.format(" %-" + widths[i] + "s |", headers[i]));
        }
        System.out.println(hRow + RESET);
        System.out.println(CYAN + top + RESET);

        if (rows == null || rows.length == 0) {
            int total = 0;
            for (int w : widths) total += w + 3;
            System.out.printf("| %-" + (total - 2) + "s|%n", "(Không có dữ liệu)");
        } else {
            for (String[] row : rows) {
                StringBuilder r = new StringBuilder("|");
                for (int i = 0; i < widths.length; i++) {
                    String cell = (i < row.length && row[i] != null) ? row[i] : "";
                    if (cell.length() > widths[i]) cell = cell.substring(0, widths[i] - 1) + "…";
                    r.append(String.format(" %-" + widths[i] + "s |", cell));
                }
                System.out.println(r);
            }
        }
        System.out.println(CYAN + top + RESET);
    }

    // ──────────────── NHẬP LIỆU ────────────────

    public static String inputString(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            error("Không được để trống, vui lòng nhập lại.");
        }
    }

    public static String inputOptional(String prompt) {
        System.out.print(YELLOW + prompt + RESET);
        return scanner.nextLine().trim();
    }

    public static int inputInt(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v > 0) return v;
                error("Vui lòng nhập số nguyên dương.");
            } catch (NumberFormatException e) {
                error("Sai định dạng số, vui lòng nhập lại.");
            }
        }
    }

    public static double inputDouble(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v >= 0) return v;
                error("Vui lòng nhập số không âm.");
            } catch (NumberFormatException e) {
                error("Sai định dạng số, vui lòng nhập lại.");
            }
        }
    }

    public static LocalDateTime inputDateTime(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + " (dd/MM/yyyy HH:mm): " + RESET);
            try {
                return LocalDateTime.parse(scanner.nextLine().trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                error("Định dạng sai. Ví dụ: 25/06/2025 09:00");
            }
        }
    }

    /** Nhập mật khẩu. Đảm bảo hoạt động cả trong IDE và Terminal. */
    public static String inputPassword(String prompt) {
        System.out.print(YELLOW + prompt + RESET);
        // Ưu tiên dùng Console nếu có (Terminal thật) để ẩn ký tự
        java.io.Console cons = System.console();
        if (cons != null) {
            char[] pwdChars = cons.readPassword();
            return (pwdChars == null) ? "" : new String(pwdChars);
        }
        // Fallback dùng Scanner (cho IDE như IntelliJ/Android Studio)
        return scanner.nextLine().trim();
    }

    public static boolean confirm(String prompt) {
        System.out.print(YELLOW + prompt + " (y/N): " + RESET);
        String s = scanner.nextLine().trim();
        return s.equalsIgnoreCase("y");
    }

    public static int readChoice(String prompt) {
        System.out.print(YELLOW + prompt + RESET);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void pause() {
        System.out.print(CYAN + "\nNhấn Enter để tiếp tục..." + RESET);
        scanner.nextLine();
    }
}
