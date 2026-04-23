package com.trading.util;

import java.util.Scanner;

/**
 * MODULE I – Handling user input/output using Scanner class
 *
 * Demonstrates the Scanner class for reading user input from the console.
 * In a Spring Boot application, this serves as a diagnostic / developer CLI
 * tool triggered from the command line during development.
 *
 * Key Scanner concepts shown:
 *  - Creating a Scanner on System.in
 *  - Reading strings (nextLine), integers (nextInt), doubles (nextDouble)
 *  - Closing the scanner (resource management)
 *
 * Platform Independence / JVM note (Module I):
 * ────────────────────────────────────────────
 * Java achieves "Write Once, Run Anywhere" through the JVM (Java Virtual Machine).
 * Source code (.java) is compiled to platform-neutral bytecode (.class).
 * Each operating system has its own JVM implementation that interprets the
 * bytecode at runtime — so this same code runs on Windows, macOS, and Linux
 * without modification.
 */
public class ConsoleDebugUtil {

    // ── Static block (Module I) ───────────────────────────────────────────────
    // Runs once when the class is first loaded by the JVM, before any static
    // method is called and before any instance is created.
    static {
        System.out.println("[ConsoleDebugUtil] Class loaded into JVM. Static block executed.");
    }

    /**
     * Interactive CLI: prompts the developer to enter a stock symbol and
     * quantity, then prints a simulated order summary.
     * Demonstrates: Scanner, while loop, if/else, String methods.
     */
    public static void runInteractiveOrderEntry() {
        // Create Scanner attached to standard input (keyboard)
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Trading Simulator — Debug Console ===");
        System.out.print("Enter stock symbol (or 'quit' to exit): ");

        String input; // Declared before the while loop (scope awareness)

        // ── while loop (Module I) ────────────────────────────────────────────
        while (scanner.hasNextLine()) {
            input = scanner.nextLine().trim().toUpperCase(); // trim, toUpperCase — Module I

            // ── if/else (Module I) ──────────────────────────────────────────
            if (input.isEmpty()) {
                System.out.print("Symbol cannot be empty. Try again: ");
                continue; // Skip rest of loop body
            }

            if (input.equals("QUIT")) {
                System.out.println("Exiting debug console.");
                break; // Exit while loop
            }

            System.out.print("Enter quantity: ");

            // ── Reading integer input ───────────────────────────────────────
            if (scanner.hasNextInt()) {
                int qty = scanner.nextInt();
                scanner.nextLine(); // Consume trailing newline

                // ── Ternary operator (Module I) ─────────────────────────────
                String validity = (qty > 0) ? "VALID" : "INVALID";

                System.out.println("Order summary: BUY " + qty + " of " + input + " — " + validity);
            } else {
                System.out.println("Invalid quantity input.");
                scanner.nextLine(); // Consume bad token
            }

            System.out.print("Enter next symbol (or 'quit'): ");
        }

        // Always close the scanner to release system resources (resource management)
        scanner.close();
        System.out.println("[ConsoleDebugUtil] Scanner closed. Goodbye.");
    }

    /**
     * Demonstrates reading a double from the console, using do-while loop.
     * do-while guarantees at least one execution of the loop body (Module I).
     */
    public static double promptForBudget() {
        Scanner scanner = new Scanner(System.in);
        double budget;

        // ── do-while loop (Module I) ─────────────────────────────────────────
        // The body runs FIRST, then the condition is checked.
        // Useful when you need at least one iteration (e.g., input prompts).
        do {
            System.out.print("Enter your trading budget (₹): ");
            while (!scanner.hasNextDouble()) {
                System.out.println("Please enter a valid number.");
                scanner.next(); // Discard bad token
            }
            budget = scanner.nextDouble();
        } while (budget <= 0); // Keep asking until a positive number is entered

        scanner.close();
        return budget;
    }
}
