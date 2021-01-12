package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


        String fileName = "byondcore.dll";
        File file = new File(fileName);
        while (!file.isFile()) {
            System.out.print("File byondcore.dll is not found, please provide exact path: ");
            fileName = reader.readLine();
            file = new File(fileName);
        }

        File patternsFile = new File("pattern.txt");
        if(patternsFile.isFile()){
            byte[][] patchStrings = readPatternsFile(patternsFile);
            patchFile(file, patchStrings);
        }
        else
        {
            System.out.println("pattern.txt file not found! So using hardcoded patterns:");
            System.out.println("You can set new patterns in pattern.txt file");
            System.out.println("Format is:");
            System.out.println("Searchfor: 00 00 00 00 32 DB 8D 8D");
            System.out.println("Changeto: 00 00 00 00 B3 01 8D 8D" + "\n");
            byte[][] patchStrings = readPredefinedPatterns("00 00 00 00 32 DB 8D 8D","00 00 00 00 B3 01 8D 8D");
            patchFile(file, patchStrings);
        }
    }



    public static void patchFile(File file, byte[][] patchStrings) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        int badByteStart = indexOf(fileBytes, patchStrings[0]);
        if (badByteStart >= 0) {
            System.arraycopy(patchStrings[1], 0, fileBytes, badByteStart, patchStrings[1].length);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileBytes);
                System.out.println("Patching done!");
            } catch (Exception e) {
                System.out.println("Something wrong");
            }
        } else {
            System.out.println("Already patched!");
        }

    }



    public static byte[][] readPatternsFile(File patternsFile) throws Exception {
            byte[] patternsStrings = Files.readAllBytes(patternsFile.toPath());
            String[] sForStrArr;
            String[] chTStrArr;

            String[] lines = patternsParsing(patternsStrings);
            sForStrArr = lines[0].split(" ");
            chTStrArr = lines[1].split(" ");

        return getBytes(sForStrArr, chTStrArr);
    }


    public static byte[][] readPredefinedPatterns(String bad, String good) throws Exception {

        String[] sForStrArr;
        String[] chTStrArr;

        sForStrArr = bad.split(" ");
        chTStrArr = good.split(" ");

        return getBytes(sForStrArr, chTStrArr);
    }

    private static byte[][] getBytes(String[] sForStrArr, String[] chTStrArr) {
        byte[] sForBytArr = new byte[sForStrArr.length];
        byte[] chTBytArr = new byte[chTStrArr.length];

        for (int i = 0; i < sForStrArr.length; i++) {
            sForBytArr[i] = hexToByte(sForStrArr[i]);
        }

        for (int i = 0; i < chTStrArr.length; i++) {
            chTBytArr[i] = hexToByte(chTStrArr[i]);
        }

        byte[][] patterns = new byte[2][2];
        patterns[0] = sForBytArr;
        patterns[1] = chTBytArr;

        return patterns;
    }


    public static String[] patternsParsing(byte[] patternsStrings) throws Exception {
        String patStr = new String(patternsStrings, StandardCharsets.UTF_8);
        String[] lines = patStr.split("\\r?\\n");
        lines[0] = lines[0].replace("Searchfor: ", ""); //delete "Searchfor: "
        lines[1] = lines[1].replace("Changeto: ", ""); //delete "Changeto: "

        if (!lines[0].contains("Searchfor: ") && lines[1].contains("Changeto: ")) {
            throw (new Exception("Wrong pattern format!"));
        }

        return lines;
    }

    //public static String byteToHex(byte num) {
    //    char[] hexDigits = new char[2];
    //    hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
    //    hexDigits[1] = Character.forDigit((num & 0xF), 16);
    //    return new String(hexDigits);
    //}

    public static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }

    /**
     * Knuth-Morris-Pratt Algorithm for Pattern Matching
     */
        public static int indexOf(byte[] data, byte[] pattern) {
            if (data.length == 0) return -1;

            int[] failure = computeFailure(pattern);
            int j = 0;

            for (int i = 0; i < data.length; i++) {
                while (j > 0 && pattern[j] != data[i]) {
                    j = failure[j - 1];
                }
                if (pattern[j] == data[i]) { j++; }
                if (j == pattern.length) {
                    return i - pattern.length + 1;
                }
            }
            return -1;
        }

        /**
         * Computes the failure function using a boot-strapping process,
         * where the pattern is matched against itself.
         */
        private static int[] computeFailure(byte[] pattern) {
            int[] failure = new int[pattern.length];

            int j = 0;
            for (int i = 1; i < pattern.length; i++) {
                while (j > 0 && pattern[j] != pattern[i]) {
                    j = failure[j - 1];
                }
                if (pattern[j] == pattern[i]) {
                    j++;
                }
                failure[i] = j;
            }

            return failure;
        }
    }



