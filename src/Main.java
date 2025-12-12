import java.io.*;
import java.util.Collections;

/**
 * lohigh - DJ Sacabambaspis lets you take lofi on the go.
 * Java implementation of the audio file combiner.
 */
public class Main {

    /**
     * Main entry point for the lohigh application.
     *
     * @param args Command line arguments
     *        - 2 args: <input_file2.wav> <output_file.wav> (uses default ambient.wav as first file)
     *        - 3 args: <input_file1.wav> <input_file2.wav> <output_file.wav>
     *        - --force flag: allows overwriting existing files without prompting
     *        - --fade=<duration> flag: applies crossfade with specified duration (e.g., --fade=1.5)
     *        - --level=<target> flag: normalizes audio to target level (e.g., --level=0.8)
     *        - --batch flag: batch process multiple files with --output-dir
     */
    public static void main(String[] args) {
        // Initialize logger with default settings
        Logger logger = new Logger();

        // Read configuration file first (command line args will override these)
        ConfigManager configManager = new ConfigManager(logger);
        java.util.Map<String, String> config = configManager.readConfigFile();

        String inputFile1 = null;
        String inputFile2 = null;
        String outputFile = null;
        boolean forceOverwrite = false;
        double fadeDuration = 0.0; // Default: no crossfade
        double normalizeLevel = 0.8; // Default: normalize to 80%
        boolean batchMode = false;
        boolean reverseMode = false;
        boolean shuffleMode = false;
        boolean dryRun = false;
        double previewDuration = 0.0; // 0 = no preview
        String outputDir = "./";
        String playlistFile = null;
        int loopCount = 1; // Default: no looping
        String ambientChoice = null; // null = use default ambient.wav
        java.util.ArrayList<String> batchFiles = new java.util.ArrayList<>();

        // Apply config file defaults
        if (config.containsKey("fade")) {
            try {
                fadeDuration = Double.parseDouble(config.get("fade"));
            } catch (NumberFormatException e) {
                logger.printVerbose("Warning: invalid fade value in config file");
            }
        }
        if (config.containsKey("level")) {
            try {
                normalizeLevel = Double.parseDouble(config.get("level"));
            } catch (NumberFormatException e) {
                logger.printVerbose("Warning: invalid level value in config file");
            }
        }
        if (config.containsKey("loop")) {
            try {
                loopCount = Integer.parseInt(config.get("loop"));
            } catch (NumberFormatException e) {
                logger.printVerbose("Warning: invalid loop value in config file");
            }
        }
        if (config.containsKey("output-dir")) {
            outputDir = config.get("output-dir");
        }
        if (config.containsKey("force")) {
            forceOverwrite = "true".equalsIgnoreCase(config.get("force"));
        }
        if (config.containsKey("reverse")) {
            reverseMode = "true".equalsIgnoreCase(config.get("reverse"));
        }
        if (config.containsKey("shuffle")) {
            shuffleMode = "true".equalsIgnoreCase(config.get("shuffle"));
        }
        if (config.containsKey("ambient")) {
            ambientChoice = config.get("ambient");
        }

        // Parse flags and file arguments (these override config file)
        int fileArgCount = 0;
        java.util.ArrayList<String> fileArgsList = new java.util.ArrayList<>();

        for (String arg : args) {
            if ("--force".equals(arg)) {
                forceOverwrite = true;
            } else if ("--reverse".equals(arg)) {
                reverseMode = true;
            } else if ("-v".equals(arg) || "--verbose".equals(arg)) {
                logger.setVerbosity(2);
            } else if ("-q".equals(arg) || "--quiet".equals(arg)) {
                logger.setVerbosity(0);
            } else if ("--json".equals(arg)) {
                logger.setJsonOutput(true);
                logger.setVerbosity(0); // Suppress normal output in JSON mode
            } else if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else if ("--shuffle".equals(arg)) {
                shuffleMode = true;
            } else if (arg.startsWith("--playlist=")) {
                playlistFile = arg.substring(11);
            } else if (arg.startsWith("--ambient=")) {
                ambientChoice = arg.substring(10);
            } else if ("--list-ambients".equals(arg)) {
                AmbientSelector ambientSelector = new AmbientSelector(logger);
                ambientSelector.listAmbientFiles();
                System.exit(0);
            } else if (arg.startsWith("--loop=")) {
                try {
                    loopCount = Integer.parseInt(arg.substring(7));
                    if (loopCount < 1) {
                        System.err.println("error: loop count must be at least 1");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("error: invalid loop count format");
                    System.err.println("suggestion: use --loop=3 (to repeat 3 times)");
                    System.exit(1);
                }
            } else if (arg.startsWith("--preview=")) {
                try {
                    String previewValue = arg.substring(10);
                    // Remove 's' suffix if present (e.g., "30s" -> "30")
                    if (previewValue.endsWith("s")) {
                        previewValue = previewValue.substring(0, previewValue.length() - 1);
                    }
                    previewDuration = Double.parseDouble(previewValue);
                    if (previewDuration <= 0) {
                        System.err.println("error: preview duration must be positive");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("error: invalid preview duration format");
                    System.err.println("suggestion: use --preview=30 or --preview=30s");
                    System.exit(1);
                }
            } else if (arg.startsWith("--fade=")) {
                try {
                    String fadeValue = arg.substring(7);
                    // Remove 's' suffix if present (e.g., "1.5s" -> "1.5")
                    if (fadeValue.endsWith("s")) {
                        fadeValue = fadeValue.substring(0, fadeValue.length() - 1);
                    }
                    fadeDuration = Double.parseDouble(fadeValue);
                    if (fadeDuration < 0) {
                        System.err.println("error: fade duration must be positive");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("error: invalid fade duration format");
                    System.err.println("suggestion: use --fade=1.5 or --fade=1.5s");
                    System.exit(1);
                }
            } else if (arg.startsWith("--level=")) {
                try {
                    String levelValue = arg.substring(8);
                    normalizeLevel = Double.parseDouble(levelValue);
                    if (normalizeLevel < 0.0 || normalizeLevel > 1.0) {
                        System.err.println("error: normalization level must be between 0.0 and 1.0");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("error: invalid normalization level format");
                    System.err.println("suggestion: use --level=0.8 (for 80% of maximum)");
                    System.exit(1);
                }
            } else if ("--no-normalize".equals(arg)) {
                normalizeLevel = -1.0; // Disable normalization
            } else if ("--batch".equals(arg)) {
                batchMode = true;
            } else if (arg.startsWith("--output-dir=")) {
                outputDir = arg.substring(13);
            } else {
                fileArgsList.add(arg);
            }
        }

        // Initialize helper classes
        AmbientSelector ambientSelector = new AmbientSelector(logger);
        PlaylistManager playlistManager = new PlaylistManager(logger);
        AudioCombiner audioCombiner = new AudioCombiner(logger);

        // Select ambient file
        String selectedAmbient = ambientSelector.selectAmbientFile(ambientChoice);

        // Handle batch mode
        if (batchMode) {
            if (fileArgsList.size() < 1) {
                System.err.println("error: batch mode requires at least one input file");
                System.err.println("usage: java Main --batch file1.wav file2.wav file3.wav --output-dir=./mixed/");
                System.exit(1);
            }

            // Shuffle files if requested
            if (shuffleMode) {
                Collections.shuffle(fileArgsList);
                logger.printVerbose("Shuffled file order for creative mixing");
            }

            // Create output directory if it doesn't exist
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                if (!outputDirFile.mkdirs()) {
                    System.err.println("error: could not create output directory: " + outputDir);
                    System.exit(1);
                }
            }

            logger.printInfo("Batch processing " + fileArgsList.size() + " file(s)...");
            int successCount = 0;
            int failCount = 0;

            for (String inputFile : fileArgsList) {
                // Generate output filename
                File inFile = new File(inputFile);
                String baseName = inFile.getName();
                int dotIndex = baseName.lastIndexOf('.');
                if (dotIndex > 0) {
                    baseName = baseName.substring(0, dotIndex);
                }
                String outFileName = baseName + "_lofi.wav";
                String outFilePath = new File(outputDir, outFileName).getPath();

                logger.printInfo("\n[" + (successCount + failCount + 1) + "/" + fileArgsList.size() + "] Processing: " + inputFile);

                // Check if output exists
                if (new File(outFilePath).exists() && !forceOverwrite) {
                    System.err.println("  Skipping: output file already exists (use --force to overwrite)");
                    failCount++;
                    continue;
                }

                // Process file
                if (audioCombiner.combineSoundFiles(selectedAmbient, inputFile, outFilePath, fadeDuration, normalizeLevel, dryRun, previewDuration, loopCount)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            logger.printInfo("\n=== Batch processing complete ===");
            logger.printInfo("  Successful: " + successCount);
            logger.printInfo("  Failed: " + failCount);
            logger.printInfo("  Total: " + fileArgsList.size());

            System.exit(failCount > 0 ? 1 : 0);
        }

        // Handle playlist mode
        if (playlistFile != null) {
            // Read playlist file
            java.util.ArrayList<String> playlistFiles = playlistManager.readPlaylist(playlistFile);
            if (playlistFiles == null || playlistFiles.isEmpty()) {
                System.err.println("error: playlist file is empty or could not be read");
                System.err.println("suggestion: ensure the playlist file contains one file path per line");
                System.exit(1);
            }

            // Shuffle if requested
            if (shuffleMode) {
                Collections.shuffle(playlistFiles);
                logger.printVerbose("Shuffled playlist order for creative mixing");
            }

            // Need an output file
            if (fileArgsList.isEmpty()) {
                System.err.println("error: playlist mode requires an output file");
                System.err.println("usage: java Main --playlist=files.txt output.wav");
                System.exit(1);
            }
            outputFile = fileArgsList.get(0);

            // Check if output exists
            if (new File(outputFile).exists() && !forceOverwrite) {
                System.err.println("error: output file '" + outputFile + "' already exists");
                System.err.println("suggestion: use a different output filename, or use --force to overwrite");
                System.exit(1);
            }

            logger.printInfo("Processing playlist with " + playlistFiles.size() + " file(s)...");

            // Process files sequentially by combining them pairwise
            String currentFile = playlistFiles.get(0);
            String tempFileBase = outputFile + ".playlist_temp_";
            int tempIndex = 0;

            for (int i = 1; i < playlistFiles.size(); i++) {
                String nextFile = playlistFiles.get(i);
                String tempOutput;

                // For the last pair, use the final output file
                if (i == playlistFiles.size() - 1) {
                    tempOutput = outputFile;
                } else {
                    tempOutput = tempFileBase + tempIndex + ".wav";
                    tempIndex++;
                }

                logger.printInfo("[" + i + "/" + (playlistFiles.size() - 1) + "] Combining: " +
                         new File(currentFile).getName() + " + " + new File(nextFile).getName());

                // Combine current with next
                if (!audioCombiner.combineSoundFiles(currentFile, nextFile, tempOutput, fadeDuration, normalizeLevel, dryRun, previewDuration, loopCount)) {
                    // Clean up temp files on failure
                    for (int j = 0; j < tempIndex; j++) {
                        new File(tempFileBase + j + ".wav").delete();
                    }
                    System.err.println("error: playlist processing failed");
                    System.exit(1);
                }

                // Delete the previous temp file if it exists
                if (i > 1) {
                    new File(tempFileBase + (tempIndex - 2) + ".wav").delete();
                }

                currentFile = tempOutput;
            }

            // Clean up any remaining temp files
            for (int j = 0; j < tempIndex; j++) {
                new File(tempFileBase + j + ".wav").delete();
            }

            logger.printInfo("\nPlaylist processing complete: " + outputFile);
            System.exit(0);
        }

        // Normal (non-batch) mode
        fileArgCount = fileArgsList.size();
        String[] fileArgs = fileArgsList.toArray(new String[0]);

        if (fileArgCount == 3) {
            // All arguments provided by user
            inputFile1 = fileArgs[0];
            inputFile2 = fileArgs[1];
            outputFile = fileArgs[2];

            // Apply reverse mode if requested
            if (reverseMode) {
                String temp = inputFile1;
                inputFile1 = inputFile2;
                inputFile2 = temp;
            }
        } else if (fileArgCount == 2) {
            // Defaults to specified lofi file
            if (reverseMode) {
                // Reverse mode: user file first, then ambient
                inputFile1 = fileArgs[0];
                inputFile2 = selectedAmbient;
            } else {
                // Normal mode: ambient first, then user file
                inputFile1 = selectedAmbient;
                inputFile2 = fileArgs[0];
            }
            outputFile = fileArgs[1];
        } else {
            // Error: incorrect number of arguments
            System.err.println("DJ Sacabambaspis cannot make music because there are an incorrect number of files.");
            System.err.println("\nUsage:");
            System.err.println("  Single file mode:");
            System.err.println("    java Main <input_file1.wav> <input_file2.wav> <output_file.wav>");
            System.err.println("    java Main <input_file2.wav> <output_file.wav>");
            System.err.println("\n  Batch mode:");
            System.err.println("    java Main --batch file1.wav file2.wav ... [--output-dir=DIR]");
            System.err.println("\n  Playlist mode:");
            System.err.println("    java Main --playlist=files.txt output.wav");
            System.err.println("\n  Stdin/stdout mode (use '-' for stdin/stdout):");
            System.err.println("    cat input.wav | java Main - - > output.wav");
            System.err.println("    java Main input.wav - > output.wav");
            System.err.println("\nOptional flags:");
            System.err.println("  --force              Overwrite output file if it already exists");
            System.err.println("  --fade=<seconds>     Apply crossfade between files (e.g., --fade=1.5)");
            System.err.println("  --level=<0.0-1.0>    Normalize audio to target level (default: 0.8)");
            System.err.println("  --no-normalize       Disable automatic volume normalization");
            System.err.println("  --reverse            Swap file order (beat after content, not before)");
            System.err.println("  -v, --verbose        Show detailed processing information");
            System.err.println("  -q, --quiet          Suppress all output except errors");
            System.err.println("  --json               Output results in JSON format for scripting");
            System.err.println("  --dry-run            Show what would be done without processing");
            System.err.println("  --preview=<seconds>  Process only first N seconds (e.g., --preview=30)");
            System.err.println("  --shuffle            Randomize file order for creative mixing");
            System.err.println("  --batch              Enable batch processing mode");
            System.err.println("  --output-dir=DIR     Output directory for batch mode (default: ./)");
            System.err.println("  --playlist=FILE      Process files from playlist (one path per line)");
            System.err.println("  --loop=N             Repeat first file N times (e.g., --loop=3)");
            System.err.println("  --ambient=NAME       Choose ambient file (ambient, vinyl, rain, cafe, night, random)");
            System.err.println("  --list-ambients      List available ambient files and exit");
            System.exit(1);
        }

        // Handle stdin/stdout
        String actualInput1 = inputFile1;
        String actualInput2 = inputFile2;
        String actualOutput = outputFile;
        boolean outputIsStdout = IOHandler.isStdio(outputFile);
        java.util.ArrayList<String> tempFiles = new java.util.ArrayList<>();

        try {
            // Handle stdin for input files
            if (IOHandler.isStdio(inputFile1)) {
                logger.printVerbose("Reading input file 1 from stdin");
                actualInput1 = IOHandler.readStdinToTempFile();
                tempFiles.add(actualInput1);
            }
            if (IOHandler.isStdio(inputFile2)) {
                logger.printVerbose("Reading input file 2 from stdin");
                actualInput2 = IOHandler.readStdinToTempFile();
                tempFiles.add(actualInput2);
            }

            // Handle stdout for output
            if (outputIsStdout) {
                // Create a temporary output file
                File tempOut = File.createTempFile("lohigh_stdout_", ".wav");
                tempOut.deleteOnExit();
                actualOutput = tempOut.getAbsolutePath();
                tempFiles.add(actualOutput);
                logger.printVerbose("Writing output to stdout");
            } else {
                // Check if output file exists and warn user (only for regular files)
                File outputFileObj = new File(outputFile);
                if (outputFileObj.exists() && !forceOverwrite) {
                    System.err.println("error: output file '" + outputFile + "' already exists");
                    System.err.println("suggestion: use a different output filename, or use --force to overwrite");
                    System.err.println("           example: java Main input.wav output.wav --force");
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.err.println("error: failed to handle stdin/stdout");
            System.err.println("  " + e.getMessage());
            System.exit(1);
        }

        boolean success = audioCombiner.combineSoundFiles(actualInput1, actualInput2, actualOutput, fadeDuration, normalizeLevel, dryRun, previewDuration, loopCount);

        // Write to stdout if needed
        if (success && outputIsStdout) {
            try {
                IOHandler.writeToStdout(actualOutput);
            } catch (IOException e) {
                System.err.println("error: failed to write to stdout");
                System.err.println("  " + e.getMessage());
                success = false;
            }
        }

        // Cleanup temp files
        for (String tempFile : tempFiles) {
            new File(tempFile).delete();
        }

        // Output JSON if requested
        if (logger.isJsonOutput()) {
            java.util.Map<String, Object> extraData = new java.util.HashMap<>();
            File outFile = new File(outputFile);
            if (outFile.exists()) {
                extraData.put("size_bytes", outFile.length());
            }
            extraData.put("fade_duration", fadeDuration);
            extraData.put("normalize_level", normalizeLevel);
            extraData.put("loop_count", loopCount);

            logger.outputJson(success, outputFile, new String[]{inputFile1, inputFile2},
                      success ? null : "Processing failed", extraData);
        }

        System.exit(success ? 0 : 1);
    }
}
