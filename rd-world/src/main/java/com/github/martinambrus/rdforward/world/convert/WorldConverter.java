package com.github.martinambrus.rdforward.world.convert;

import java.io.File;

/**
 * CLI tool for converting worlds between RubyDung, Alpha, and McRegion formats.
 *
 * Usage:
 *   java -jar rd-world.jar convert <input> <output-dir> <target-format>
 *
 * Input format is auto-detected via {@link WorldFormatDetector}.
 * Multi-step conversions (e.g. RubyDung to McRegion) are chained
 * automatically via {@link ConversionRegistry}.
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
        "Input format is auto-detected:\n" +
        "  .dat file    -> RubyDung (server or original format, detected by header)\n" +
        "  directory    -> Alpha or McRegion world (detected by contents)\n" +
        "\n" +
        "Target formats:\n" +
        "  alpha              -> Minecraft Alpha individual chunk files\n" +
        "  region|mcregion|mcr -> McRegion .mcr files (Beta 1.3+)\n" +
        "  server|rdserver    -> RDForward server-world.dat format\n" +
        "  rubydung|rd        -> Original RubyDung level.dat format\n" +
        "\n" +
        "Options:\n" +
        "  --seed <n>   -> World seed for level.dat (default: 0)\n" +
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
        String targetFormatArg = args[argOffset + 2].toLowerCase();

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

        // Auto-detect source format
        WorldFormat sourceFormat = WorldFormatDetector.detect(input);
        if (sourceFormat == null) {
            System.err.println("Error: cannot determine input format.");
            System.err.println("Expected a RubyDung .dat file, Alpha world directory, "
                    + "or McRegion world directory.");
            System.exit(1);
        }

        // Parse target format
        WorldFormat targetFormat = parseTargetFormat(targetFormatArg);
        if (targetFormat == null) {
            System.err.println("Error: unknown target format '" + targetFormatArg + "'");
            System.err.println("Supported formats: alpha, region, server, rubydung");
            System.exit(1);
        }

        // Find and execute conversion path
        ConversionRegistry registry = ConversionRegistry.createDefault();

        try {
            System.out.println("=== " + sourceFormat + " -> " + targetFormat + " conversion ===");
            registry.convert(input, output, sourceFormat, targetFormat, seed);
            System.out.println("=== Conversion complete ===");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static WorldFormat parseTargetFormat(String arg) {
        switch (arg) {
            case "alpha":
                return WorldFormat.ALPHA;
            case "region":
            case "mcregion":
            case "mcr":
                return WorldFormat.MCREGION;
            case "server":
            case "rdserver":
                return WorldFormat.RUBYDUNG_SERVER;
            case "rubydung":
            case "rd":
                return WorldFormat.RUBYDUNG;
            default:
                return null;
        }
    }
}
