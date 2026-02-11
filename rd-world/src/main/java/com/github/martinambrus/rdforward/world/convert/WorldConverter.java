package com.github.martinambrus.rdforward.world.convert;

import java.io.File;

/**
 * CLI tool for converting worlds between RubyDung, Alpha, and McRegion formats.
 *
 * Usage:
 *   java -jar rd-world.jar convert <input> <output-dir> <target-format>
 *
 * Supported conversions:
 *   rubydung-to-alpha   — server-world.dat → Alpha chunk directory
 *   alpha-to-region     — Alpha chunk directory → McRegion .mcr files
 *   rubydung-to-region  — server-world.dat → McRegion .mcr files (two-step)
 *
 * Input auto-detection:
 *   - If input is a .dat file → treated as RubyDung server-world.dat
 *   - If input is a directory → treated as Alpha world directory
 *
 * Target formats:
 *   alpha   — Minecraft Alpha individual chunk files (pre-Beta 1.3)
 *   region  — McRegion .mcr files (Beta 1.3+)
 *
 * Options:
 *   --seed <value>  — world seed for level.dat (default: 0)
 *
 * Examples:
 *   java -jar rd-world.jar convert server-world.dat ./alpha-world alpha
 *   java -jar rd-world.jar convert ./alpha-world ./region-world region
 *   java -jar rd-world.jar convert server-world.dat ./region-world region --seed 12345
 */
public class WorldConverter {

    private static final String USAGE =
        "Usage: java -jar rd-world.jar convert <input> <output-dir> <target-format> [options]\n" +
        "\n" +
        "Converts worlds between RubyDung, Alpha, and McRegion formats.\n" +
        "\n" +
        "Input (auto-detected):\n" +
        "  .dat file    → RubyDung server-world.dat\n" +
        "  directory    → Alpha chunk world\n" +
        "\n" +
        "Target formats:\n" +
        "  alpha        → Minecraft Alpha individual chunk files\n" +
        "  region       → McRegion .mcr files (Beta 1.3+)\n" +
        "\n" +
        "Options:\n" +
        "  --seed <n>   → World seed for level.dat (default: 0)\n" +
        "\n" +
        "Examples:\n" +
        "  convert server-world.dat ./alpha-world alpha\n" +
        "  convert ./alpha-world ./region-world region\n" +
        "  convert server-world.dat ./region-world region --seed 12345";

    public static void main(String[] args) {
        if (args.length < 1 || "help".equals(args[0]) || "--help".equals(args[0])) {
            System.out.println(USAGE);
            return;
        }

        // Skip the "convert" subcommand if present
        int argOffset = 0;
        if ("convert".equals(args[0])) {
            argOffset = 1;
        }

        if (args.length - argOffset < 3) {
            System.err.println("Error: expected 3 arguments: <input> <output-dir> <target-format>");
            System.err.println();
            System.out.println(USAGE);
            System.exit(1);
        }

        String inputPath = args[argOffset];
        String outputPath = args[argOffset + 1];
        String targetFormat = args[argOffset + 2].toLowerCase();

        // Parse options
        long seed = 0;
        for (int i = argOffset + 3; i < args.length; i++) {
            if ("--seed".equals(args[i]) && i + 1 < args.length) {
                try {
                    seed = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid seed: " + args[i]);
                    System.exit(1);
                }
            }
        }

        File input = new File(inputPath);
        File output = new File(outputPath);

        if (!input.exists()) {
            System.err.println("Error: input not found: " + input.getAbsolutePath());
            System.exit(1);
        }

        // Detect input format
        boolean isRubyDung = input.isFile() && input.getName().endsWith(".dat");
        boolean isAlpha = input.isDirectory();

        if (!isRubyDung && !isAlpha) {
            System.err.println("Error: cannot determine input format.");
            System.err.println("Expected a .dat file (RubyDung) or directory (Alpha world).");
            System.exit(1);
        }

        try {
            switch (targetFormat) {
                case "alpha":
                    if (!isRubyDung) {
                        System.err.println("Error: 'alpha' target only supports RubyDung .dat input.");
                        System.exit(1);
                    }
                    convertRubyDungToAlpha(input, output, seed);
                    break;

                case "region":
                case "mcregion":
                case "mcr":
                    if (isRubyDung) {
                        // Two-step: RubyDung → Alpha (temp) → Region
                        convertRubyDungToRegion(input, output, seed);
                    } else {
                        convertAlphaToRegion(input, output);
                    }
                    break;

                default:
                    System.err.println("Error: unknown target format '" + targetFormat + "'");
                    System.err.println("Supported formats: alpha, region");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void convertRubyDungToAlpha(File input, File output, long seed) throws Exception {
        System.out.println("=== RubyDung → Alpha conversion ===");
        new RubyDungToAlphaConverter().convert(input, output, seed);
    }

    private static void convertAlphaToRegion(File input, File output) throws Exception {
        System.out.println("=== Alpha → McRegion conversion ===");
        new McRegionWriter().convertAlphaToRegion(input, output);
    }

    private static void convertRubyDungToRegion(File input, File output, long seed) throws Exception {
        System.out.println("=== RubyDung → McRegion conversion (via Alpha) ===");

        // Step 1: Convert to Alpha in a temp directory
        File tempAlpha = new File(output.getParentFile(),
            ".rdforward-convert-temp-" + System.currentTimeMillis());
        try {
            System.out.println();
            System.out.println("Step 1/2: RubyDung → Alpha");
            new RubyDungToAlphaConverter().convert(input, tempAlpha, seed);

            // Step 2: Convert Alpha to McRegion
            System.out.println();
            System.out.println("Step 2/2: Alpha → McRegion");
            new McRegionWriter().convertAlphaToRegion(tempAlpha, output);
        } finally {
            // Clean up temp directory
            deleteRecursive(tempAlpha);
        }

        System.out.println();
        System.out.println("=== Conversion complete ===");
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
