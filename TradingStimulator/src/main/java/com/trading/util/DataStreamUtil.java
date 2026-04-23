package com.trading.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE II – Data Stream Handling
 *
 * Java provides two families of I/O streams:
 *
 *  ┌─────────────────────────────────────────────────────────────────────┐
 *  │  BYTE STREAMS (binary data)           CHAR STREAMS (text data)      │
 *  │  InputStream / OutputStream           Reader / Writer               │
 *  │  FileInputStream / FileOutputStream   FileReader / FileWriter        │
 *  │  BufferedInputStream                  BufferedReader / BufferedWriter│
 *  │  ObjectInputStream (serialisation)    PrintWriter                   │
 *  └─────────────────────────────────────────────────────────────────────┘
 *
 * All streams implement Closeable — use try-with-resources (Module II / III)
 * to guarantee streams are always closed, preventing resource leaks.
 *
 * In the Trading Simulator, streams are used to:
 *  - Export trade history to CSV files (byte / char output stream)
 *  - Read configuration or batch order files (char input stream)
 *  - Serialise/deserialise Order objects (object stream)
 */
public class DataStreamUtil {

    // ── Byte Output Stream — write raw bytes to a file ────────────────────────
    /**
     * Writes a trade report as raw bytes using FileOutputStream (byte stream).
     * FileOutputStream wraps a file and writes bytes one-by-one or in arrays.
     * BufferedOutputStream wraps it for efficiency (fewer disk I/O calls).
     *
     * @param filePath  absolute or relative path of the output file
     * @param content   the text content to write (converted to bytes)
     */
    public static void writeTradeReportBytes(String filePath, String content) {
        // try-with-resources — stream closed automatically (Module II)
        try (OutputStream fileOut = new FileOutputStream(filePath);
             OutputStream buffered = new BufferedOutputStream(fileOut)) {

            // Convert String to byte array using UTF-8 encoding
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            // write(byte[]) — writes the entire byte array to the stream
            buffered.write(bytes);

            // flush() forces any buffered bytes to the underlying stream
            buffered.flush();
            System.out.println("[DataStream] Report written to: " + filePath
                    + " (" + bytes.length + " bytes)");

        } catch (IOException e) {
            // IOException is a CHECKED exception — must be caught (Module II)
            System.err.println("[DataStream] Failed to write report: " + e.getMessage());
        }
    }

    // ── Byte Input Stream — read raw bytes from a file ────────────────────────
    /**
     * Reads a file as raw bytes using FileInputStream (byte stream).
     * Returns the full file content as a String.
     *
     * @param filePath  path of the file to read
     * @return          file content as a String, or empty string on error
     */
    public static String readFileBytes(String filePath) {
        StringBuilder sb = new StringBuilder();

        try (InputStream fileIn   = new FileInputStream(filePath);
             InputStream buffered = new BufferedInputStream(fileIn)) {

            byte[] buffer = new byte[1024]; // Read in 1 KB chunks
            int    bytesRead;

            // read(buffer) fills the buffer and returns how many bytes were read.
            // Returns -1 at end of stream.
            while ((bytesRead = buffered.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }

        } catch (FileNotFoundException e) {
            // FileNotFoundException — subclass of IOException (checked)
            System.err.println("[DataStream] File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("[DataStream] Read error: " + e.getMessage());
        }

        return sb.toString();
    }

    // ── Character Output Stream — write text (CSV trade export) ───────────────
    /**
     * Exports a list of trade summaries to a CSV file using PrintWriter.
     * PrintWriter is a character stream — handles encoding automatically.
     * BufferedWriter behind it reduces the number of physical writes.
     *
     * @param filePath  path of the CSV output file
     * @param trades    list of "symbol,qty,price" strings to write
     */
    public static void exportTradesToCsv(String filePath, List<String> trades) {
        // PrintWriter wraps BufferedWriter wraps FileWriter — character stream chain
        try (FileWriter fw  = new FileWriter(filePath, StandardCharsets.UTF_8, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            // Write CSV header
            pw.println("Symbol,Quantity,Price,Total");  // println adds newline

            for (String tradeEntry : trades) {
                // Each entry is "SYMBOL:QTY:PRICE"
                String[] parts = tradeEntry.split(":");
                if (parts.length == 3) {
                    String symbol = parts[0];
                    int    qty    = Integer.parseInt(parts[1]);        // Wrapper: parseInt
                    double price  = Double.parseDouble(parts[2]);      // Wrapper: parseDouble
                    double total  = qty * price;

                    // printf formats the line with precise decimal control
                    pw.printf("%s,%d,%.2f,%.2f%n", symbol, qty, price, total);
                }
            }

            // PrintWriter.checkError() lets you detect silent write failures
            if (pw.checkError()) {
                System.err.println("[DataStream] A write error occurred in PrintWriter.");
            } else {
                System.out.println("[DataStream] CSV exported: " + filePath
                        + " (" + trades.size() + " trades)");
            }

        } catch (IOException e) {
            System.err.println("[DataStream] CSV export failed: " + e.getMessage());
        }
    }

    // ── Character Input Stream — read CSV orders back ─────────────────────────
    /**
     * Reads a CSV file line by line using BufferedReader (character stream).
     * BufferedReader.readLine() is the standard efficient way to read text.
     *
     * @param filePath  path of the CSV file to read
     * @return          list of non-empty, non-header lines
     */
    public static List<String> readCsvLines(String filePath) {
        List<String> lines = new ArrayList<>();

        try (FileReader fr = new FileReader(filePath, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            boolean firstLine = true;

            // readLine() reads one text line at a time — returns null at EOF
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header row
                    continue;
                }
                line = line.trim(); // trim() — Module I String method
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }

        } catch (IOException e) {
            System.err.println("[DataStream] CSV read failed: " + e.getMessage());
        }

        return lines;
    }

    // ── Object Serialization Stream (ObjectOutputStream / ObjectInputStream) ───
    /**
     * Demonstrates Java Object Serialisation — converting an object to a byte
     * stream so it can be saved to disk or sent over the network.
     *
     * Serialisation requires the class to implement java.io.Serializable (marker interface).
     * ObjectOutputStream writes the object; ObjectInputStream reads it back.
     *
     * SerializableTrade is a simple serialisable record for the demo.
     */
    public static class SerializableTrade implements Serializable {
        // serialVersionUID ensures version compatibility during deserialisation
        private static final long serialVersionUID = 1L;

        public final String symbol;
        public final int    quantity;
        public final double price;

        public SerializableTrade(String symbol, int quantity, double price) {
            this.symbol   = symbol;
            this.quantity = quantity;
            this.price    = price;
        }

        @Override
        public String toString() {
            return symbol + " × " + quantity + " @ ₹" + price;
        }
    }

    /** Serialises a SerializableTrade to a binary file */
    public static void serializeTrade(String filePath, SerializableTrade trade) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(trade); // Writes the full object graph as bytes
            System.out.println("[DataStream] Serialised trade to: " + filePath);
        } catch (IOException e) {
            System.err.println("[DataStream] Serialisation error: " + e.getMessage());
        }
    }

    /** Deserialises a SerializableTrade from a binary file */
    public static SerializableTrade deserializeTrade(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            // readObject() returns Object — must be cast (unchecked cast)
            return (SerializableTrade) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Multi-catch (Module II) — handles two different exception types
            System.err.println("[DataStream] Deserialisation error: " + e.getMessage());
        }
        return null;
    }
}
