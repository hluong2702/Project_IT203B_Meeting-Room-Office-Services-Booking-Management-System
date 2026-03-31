package com.meetingroom.util;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

 public static void main(String[] args) {
            String plainPassword = "Admin@123";
            String hashed = PasswordUtil.hash(plainPassword);
            System.out.println("Hash: " + hashed);

            boolean verified = PasswordUtil.verify(plainPassword, hashed);
            System.out.println("Verify: " + verified);
        }
}