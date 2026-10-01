package com.m1kro.burploader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * M1kro Loader - standalone keygen CLI (no GUI, no network).
 *
 * Two entry points share this jar:
 *   - run directly  (java -jar loader.jar ...) -> this class, the offline keygen
 *   - as java agent (-javaagent:loader.jar)    -> Loader.premain, the runtime patcher
 *
 * This CLI never opens a socket, never reads/writes files, and never disables
 * TLS verification. It only reads activation requests from stdin and prints
 * the license text and activation responses to stdout.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String licenseName = "M1kro";
        boolean licenseOnly = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                case "--name":
                    if (i + 1 < args.length) {
                        licenseName = args[++i];
                    }
                    break;
                case "--license-only":
                    licenseOnly = true;
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    return;
                default:
                    break;
            }
        }

        System.out.println("=== M1kro Loader & Keygen (offline) ===");
        System.out.println();
        System.out.println("License name: " + licenseName);
        System.out.println("--- License text (Burp > Settings > ... > License, paste this) ---");
        System.out.println(Keygen.generateLicense(licenseName));
        System.out.println("------------------------------------------------------------------");

        if (licenseOnly) {
            return;
        }

        System.out.println();
        System.out.println("Burp will then ask for manual activation and show a request string.");
        System.out.println("Paste that activation request below and press Enter (Ctrl+D to quit):");
        System.out.println();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            String request = line.trim();
            if (request.isEmpty()) {
                continue;
            }
            try {
                String response = Keygen.generateActivation(request);
                System.out.println();
                System.out.println("--- Activation response (paste back into Burp) ---");
                System.out.println(response);
                System.out.println("---------------------------------------------------");
            } catch (Exception e) {
                System.out.println("Could not process that activation request: " + e.getMessage());
            }
            System.out.println();
            System.out.println("Paste another activation request, or press Ctrl+D to quit:");
        }
    }

    private static void printHelp() {
        System.out.println("M1kro Loader & Keygen");
        System.out.println();
        System.out.println("Run as keygen:  java -jar loader.jar [--name NAME] [--license-only]");
        System.out.println("Run as agent :  java -javaagent:loader.jar -jar burpsuite_pro.jar");
        System.out.println();
        System.out.println("  -n, --name NAME     Name to embed in the generated license (default: M1kro)");
        System.out.println("      --license-only  Only print the license text, then exit");
        System.out.println("  -h, --help          Show this help");
    }
}
