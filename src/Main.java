import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.Collections;

/**
 * lohigh - DJ Sacabambaspis lets you take lofi on the go.
 * Java implementation of the audio file combiner.
 */
public class Main {

    private static final String DEFAULT_INPUT_FILE1 = "../asset/ambient.wav";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1GB default limit

    // Verbosity levels
    private static int verbosity = 1; // 0 = quiet, 1 = normal, 2 = verbose

    /**
     * Prints info message if verbosity level allows.
     */
    private static void printInfo(String message) {
        if (verbosity >= 1) {
            System.out.println(message);
        }
    }

    /**
     * Prints verbose message if verbosity level allows.
     */
    private static void printVerbose(String message) {
        if (verbosity >= 2) {
            System.out.println("[VERBOSE] " + message);
        }
    }

    /**
     * Prints error message (always shown).
     */
    private static void printError(String message) {
        System.err.println(message);
    }

    /**
     * Prints a progress bar.
     *
     * @param current Current progress value
     * @param total Total value
     * @param operation Description of operation
     */
    private static void printProgress(long current, long total, String operation) {
        if (verbosity < 1) return; // Don't show in quiet mode

        int percent = (int) ((current * 100) / total);
        int barLength = 40;
        int filled = (int) ((current * barLength) / total);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("=");
            } else if (i == filled) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");

        // Use \r to overwrite the same line
        System.out.print("\r" + operation + ": " + bar + " " + percent + "%");
        if (current >= total) {
            System.out.println(); // New line when complete
        }
    }

    /**
     * Validates an input audio file for common issues.
     *
     * @param filePath Path to the audio file to validate
     * @return true if valid, false otherwise
     */
    private static boolean validateInputFile(String filePath) {
        File file = new File(filePath);

        // Check if file exists
        if (!file.exists()) {
            System.err.println("error: cannot open '" + filePath + "' - file not found");
            System.err.println("suggestion: check the file path and try again");
            return false;
        }

        // Check if file is readable
        if (!file.canRead()) {
            System.err.println("error: cannot read '" + filePath + "' - permission denied");
            System.err.println("suggestion: check file permissions (chmod +r " + filePath + ")");
            return false;
        }

        // Check file size
        long fileSize = file.length();
        if (fileSize == 0) {
            System.err.println("error: '" + filePath + "' is empty (0 bytes)");
            System.err.println("suggestion: ensure the file contains valid audio data");
            return false;
        }

        if (fileSize > MAX_FILE_SIZE) {
            System.err.println("error: '" + filePath + "' is too large (" + (fileSize / 1024 / 1024) + " MB)");
            System.err.println("suggestion: file exceeds maximum size of " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
            return false;
        }

        // Validate it's a proper audio file
        try {
            AudioInputStream testStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = testStream.getFormat();

            // Check for non-zero duration
            long frames = testStream.getFrameLength();
            if (frames <= 0) {
                System.err.println("error: '" + filePath + "' has invalid duration");
                System.err.println("suggestion: ensure the file contains valid audio frames");
                testStream.close();
                return false;
            }

            testStream.close();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("error: '" + filePath + "' is not a valid audio file");
            System.err.println("suggestion: ensure the file is in WAV format and not corrupted");
            System.err.println("           try converting with: ffmpeg -i input.mp3 output.wav");
            return false;
        } catch (IOException e) {
            System.err.println("error: cannot read '" + filePath + "' - " + e.getMessage());
            System.err.println("suggestion: check if the file is corrupted or in use by another program");
            return false;
        }

        return true;
    }

    /**
     * Checks if there's sufficient disk space for the output file.
     *
     * @param outputPath Path to the output file
     * @param estimatedSize Estimated size of the output file in bytes
     * @return true if sufficient space, false otherwise
     */
    private static boolean checkDiskSpace(String outputPath, long estimatedSize) {
        try {
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir == null) {
                parentDir = new File(".");
            }

            long freeSpace = parentDir.getUsableSpace();
            long requiredSpace = estimatedSize + (100L * 1024L * 1024L); // Add 100MB buffer

            if (freeSpace < requiredSpace) {
                System.err.println("error: insufficient disk space for output file");
                System.err.println("  required: " + (requiredSpace / 1024 / 1024) + " MB");
                System.err.println("  available: " + (freeSpace / 1024 / 1024) + " MB");
                System.err.println("suggestion: free up disk space or choose a different output location");
                return false;
            }
        } catch (Exception e) {
            // If we can't check disk space, just warn and continue
            System.err.println("warning: could not verify available disk space");
        }

        return true;
    }

    /**
     * Finds the peak audio level in a byte array.
     *
     * @param audioData The audio data to analyze
     * @param format Audio format for sample interpretation
     * @return Peak level as a value between 0.0 and 1.0
     */
    private static double findPeakLevel(byte[] audioData, AudioFormat format) {
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        boolean bigEndian = format.isBigEndian();
        int maxAmplitude = 0;

        for (int i = 0; i < audioData.length - bytesPerSample; i += bytesPerSample) {
            int sample = 0;

            if (bytesPerSample == 2) {
                // 16-bit audio
                if (bigEndian) {
                    sample = (audioData[i] << 8) | (audioData[i + 1] & 0xFF);
                } else {
                    sample = (audioData[i + 1] << 8) | (audioData[i] & 0xFF);
                }
            }

            int amplitude = Math.abs(sample);
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude;
            }
        }

        // Return as fraction of maximum possible amplitude (32767 for 16-bit)
        return maxAmplitude / 32767.0;
    }

    /**
     * Normalizes audio data to a target peak level.
     *
     * @param audioData The audio data to normalize
     * @param format Audio format for sample interpretation
     * @param targetLevel Target peak level (0.0 to 1.0, typically 0.8)
     * @return Normalized audio bytes
     */
    private static byte[] normalizeAudio(byte[] audioData, AudioFormat format, double targetLevel) {
        // Find current peak level
        double currentPeak = findPeakLevel(audioData, format);

        if (currentPeak < 0.001) {
            // Audio is essentially silent, don't normalize
            return audioData;
        }

        // Calculate scaling factor
        double scaleFactor = targetLevel / currentPeak;

        // Don't amplify if already at or above target
        if (scaleFactor > 1.0) {
            scaleFactor = Math.min(scaleFactor, 1.0 / currentPeak); // Prevent clipping
        } else {
            // Already loud enough, no change needed
            return audioData;
        }

        int bytesPerSample = format.getSampleSizeInBits() / 8;
        boolean bigEndian = format.isBigEndian();
        byte[] normalized = new byte[audioData.length];

        for (int i = 0; i < audioData.length - bytesPerSample; i += bytesPerSample) {
            int sample = 0;

            if (bytesPerSample == 2) {
                // 16-bit audio
                if (bigEndian) {
                    sample = (audioData[i] << 8) | (audioData[i + 1] & 0xFF);
                } else {
                    sample = (audioData[i + 1] << 8) | (audioData[i] & 0xFF);
                }

                // Apply scaling
                sample = (int) (sample * scaleFactor);

                // Clamp to 16-bit range
                sample = Math.max(-32768, Math.min(32767, sample));

                // Write back
                if (bigEndian) {
                    normalized[i] = (byte) (sample >> 8);
                    normalized[i + 1] = (byte) (sample & 0xFF);
                } else {
                    normalized[i] = (byte) (sample & 0xFF);
                    normalized[i + 1] = (byte) (sample >> 8);
                }
            }
        }

        return normalized;
    }

    /**
     * Applies linear crossfade between two audio byte arrays.
     *
     * @param fadeBuffer1 The ending portion of the first audio file
     * @param fadeBuffer2 The starting portion of the second audio file
     * @param format Audio format for sample interpretation
     * @return Crossfaded audio bytes
     */
    private static byte[] applyCrossfade(byte[] fadeBuffer1, byte[] fadeBuffer2, AudioFormat format) {
        int fadeLength = Math.min(fadeBuffer1.length, fadeBuffer2.length);
        byte[] result = new byte[fadeLength];

        int bytesPerSample = format.getSampleSizeInBits() / 8;
        boolean bigEndian = format.isBigEndian();

        for (int i = 0; i < fadeLength; i += bytesPerSample) {
            // Calculate fade factor (0.0 to 1.0)
            float fadeFactor = (float) i / fadeLength;

            // Read samples from both buffers
            int sample1 = 0, sample2 = 0;

            if (bytesPerSample == 2) {
                // 16-bit audio
                if (bigEndian) {
                    sample1 = (fadeBuffer1[i] << 8) | (fadeBuffer1[i + 1] & 0xFF);
                    sample2 = (fadeBuffer2[i] << 8) | (fadeBuffer2[i + 1] & 0xFF);
                } else {
                    sample1 = (fadeBuffer1[i + 1] << 8) | (fadeBuffer1[i] & 0xFF);
                    sample2 = (fadeBuffer2[i + 1] << 8) | (fadeBuffer2[i] & 0xFF);
                }
            }

            // Apply crossfade: fade out first, fade in second
            int mixed = (int) ((sample1 * (1.0f - fadeFactor)) + (sample2 * fadeFactor));

            // Clamp to 16-bit range
            mixed = Math.max(-32768, Math.min(32767, mixed));

            // Write back to result buffer
            if (bytesPerSample == 2) {
                if (bigEndian) {
                    result[i] = (byte) (mixed >> 8);
                    result[i + 1] = (byte) (mixed & 0xFF);
                } else {
                    result[i] = (byte) (mixed & 0xFF);
                    result[i + 1] = (byte) (mixed >> 8);
                }
            }
        }

        return result;
    }

    /**
     * Combines two sound files into one output file by concatenating them.
     *
     * @param inputFile1 Path to the first input WAV file
     * @param inputFile2 Path to the second input WAV file
     * @param outputFile Path to the output WAV file
     * @param fadeDurationSeconds Duration of crossfade in seconds (0 for no crossfade)
     * @param normalizeLevel Target normalization level (0.0 to 1.0, or -1 to disable)
     * @param dryRun If true, only show what would be done without processing
     * @param previewDuration If > 0, only process first N seconds of each file
     * @return true if successful, false otherwise
     */
    public static boolean combineSoundFiles(String inputFile1, String inputFile2, String outputFile, double fadeDurationSeconds, double normalizeLevel, boolean dryRun, double previewDuration) {
        // Validate input files
        if (!validateInputFile(inputFile1)) {
            return false;
        }
        if (!validateInputFile(inputFile2)) {
            return false;
        }

        // Dry run mode: just show metadata and exit
        if (dryRun) {
            try {
                File file1 = new File(inputFile1);
                File file2 = new File(inputFile2);
                AudioInputStream stream1 = AudioSystem.getAudioInputStream(file1);
                AudioInputStream stream2 = AudioSystem.getAudioInputStream(file2);
                AudioFormat fmt1 = stream1.getFormat();
                AudioFormat fmt2 = stream2.getFormat();

                printInfo("=== DRY RUN MODE ===");
                printInfo("\nInput File 1: " + inputFile1);
                printInfo("  Size: " + (file1.length() / 1024) + " KB");
                printInfo("  Duration: " + String.format("%.2f", stream1.getFrameLength() / fmt1.getFrameRate()) + " seconds");
                printInfo("  Sample Rate: " + (int)fmt1.getSampleRate() + " Hz");
                printInfo("  Channels: " + fmt1.getChannels());
                printInfo("  Bit Depth: " + fmt1.getSampleSizeInBits() + " bits");

                printInfo("\nInput File 2: " + inputFile2);
                printInfo("  Size: " + (file2.length() / 1024) + " KB");
                printInfo("  Duration: " + String.format("%.2f", stream2.getFrameLength() / fmt2.getFrameRate()) + " seconds");
                printInfo("  Sample Rate: " + (int)fmt2.getSampleRate() + " Hz");
                printInfo("  Channels: " + fmt2.getChannels());
                printInfo("  Bit Depth: " + fmt2.getSampleSizeInBits() + " bits");

                long estimatedSize = file1.length() + file2.length();
                printInfo("\nOutput File: " + outputFile);
                printInfo("  Estimated Size: " + (estimatedSize / 1024) + " KB");
                printInfo("  Estimated Duration: " + String.format("%.2f",
                    (stream1.getFrameLength() + stream2.getFrameLength()) / fmt1.getFrameRate()) + " seconds");

                printInfo("\nSettings:");
                printInfo("  Crossfade: " + (fadeDurationSeconds > 0 ? fadeDurationSeconds + " seconds" : "disabled"));
                printInfo("  Normalization: " + (normalizeLevel > 0 ? String.format("%.1f%%", normalizeLevel * 100) : "disabled"));

                stream1.close();
                stream2.close();
                printInfo("\nNo files were modified (dry run).");
                return true;
            } catch (Exception e) {
                printError("error: could not read file metadata for dry run");
                return false;
            }
        }

        AudioInputStream audioStream1 = null;
        AudioInputStream audioStream2 = null;
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();

        try {
            // Read first file
            File file1 = new File(inputFile1);
            audioStream1 = AudioSystem.getAudioInputStream(file1);

            // Read second file
            File file2 = new File(inputFile2);
            audioStream2 = AudioSystem.getAudioInputStream(file2);

            // Get audio format from first file (this will be the output format)
            AudioFormat format = audioStream1.getFormat();

            // Check if formats are compatible
            AudioFormat format2 = audioStream2.getFormat();
            if (!format.matches(format2)) {
                System.err.println("error: audio format mismatch between input files");
                System.err.println("  File 1 (" + inputFile1 + "):");
                System.err.println("    Sample Rate: " + format.getSampleRate() + " Hz");
                System.err.println("    Channels: " + format.getChannels());
                System.err.println("    Bit Depth: " + format.getSampleSizeInBits() + " bits");
                System.err.println("  File 2 (" + inputFile2 + "):");
                System.err.println("    Sample Rate: " + format2.getSampleRate() + " Hz");
                System.err.println("    Channels: " + format2.getChannels());
                System.err.println("    Bit Depth: " + format2.getSampleSizeInBits() + " bits");
                System.err.println("suggestion: convert files to matching format using ffmpeg:");
                System.err.println("           ffmpeg -i input.wav -ar " + (int)format.getSampleRate() +
                                 " -ac " + format.getChannels() + " output.wav");
                audioStream1.close();
                audioStream2.close();
                return false;
            }

            // Estimate output file size and check disk space
            long file1Size = file1.length();
            long file2Size = file2.length();
            long estimatedOutputSize = file1Size + file2Size;

            if (!checkDiskSpace(outputFile, estimatedOutputSize)) {
                audioStream1.close();
                audioStream2.close();
                return false;
            }

            // Calculate fade length in bytes
            int fadeFrames = (int) (fadeDurationSeconds * format.getSampleRate());
            int fadeLengthBytes = fadeFrames * format.getFrameSize();

            // Calculate preview limit if needed
            long maxFrames1 = audioStream1.getFrameLength();
            long maxFrames2 = audioStream2.getFrameLength();
            if (previewDuration > 0) {
                long previewFrames = (long)(previewDuration * format.getSampleRate());
                maxFrames1 = Math.min(maxFrames1, previewFrames);
                maxFrames2 = Math.min(maxFrames2, previewFrames);
                printVerbose("Preview mode: limiting to " + previewDuration + " seconds per file");
                printVerbose("  File 1: " + maxFrames1 + " frames");
                printVerbose("  File 2: " + maxFrames2 + " frames");
            }

            // Read first file into buffer (with preview limit)
            ByteArrayOutputStream buffer1 = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[8192];
            int bytesRead;
            long bytesReadTotal1 = 0;
            long maxBytes1 = maxFrames1 * format.getFrameSize();
            long file1ActualSize = new File(inputFile1).length();
            boolean showProgress1 = file1ActualSize > 10 * 1024 * 1024; // Show for files > 10MB

            while ((bytesRead = audioStream1.read(tempBuffer)) != -1 && bytesReadTotal1 < maxBytes1) {
                int toWrite = (int)Math.min(bytesRead, maxBytes1 - bytesReadTotal1);
                buffer1.write(tempBuffer, 0, toWrite);
                bytesReadTotal1 += toWrite;

                if (showProgress1 && bytesReadTotal1 % (1024 * 1024) == 0) { // Update every MB
                    printProgress(bytesReadTotal1, Math.min(maxBytes1, file1ActualSize), "Reading file 1");
                }
            }
            if (showProgress1) {
                printProgress(bytesReadTotal1, Math.min(maxBytes1, file1ActualSize), "Reading file 1");
            }
            byte[] audio1 = buffer1.toByteArray();

            // Read second file into buffer (with preview limit)
            ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
            long bytesReadTotal2 = 0;
            long maxBytes2 = maxFrames2 * format.getFrameSize();
            long file2ActualSize = new File(inputFile2).length();
            boolean showProgress2 = file2ActualSize > 10 * 1024 * 1024; // Show for files > 10MB

            while ((bytesRead = audioStream2.read(tempBuffer)) != -1 && bytesReadTotal2 < maxBytes2) {
                int toWrite = (int)Math.min(bytesRead, maxBytes2 - bytesReadTotal2);
                buffer2.write(tempBuffer, 0, toWrite);
                bytesReadTotal2 += toWrite;

                if (showProgress2 && bytesReadTotal2 % (1024 * 1024) == 0) { // Update every MB
                    printProgress(bytesReadTotal2, Math.min(maxBytes2, file2ActualSize), "Reading file 2");
                }
            }
            if (showProgress2) {
                printProgress(bytesReadTotal2, Math.min(maxBytes2, file2ActualSize), "Reading file 2");
            }
            byte[] audio2 = buffer2.toByteArray();

            if (previewDuration > 0) {
                printInfo("Preview mode: processed " + previewDuration + " seconds from each file");
            }

            // Apply normalization if enabled
            if (normalizeLevel > 0) {
                double peak1 = findPeakLevel(audio1, format);
                double peak2 = findPeakLevel(audio2, format);

                printVerbose("Pre-normalization levels:");
                printVerbose("  File 1 peak: " + String.format("%.1f%%", peak1 * 100));
                printVerbose("  File 2 peak: " + String.format("%.1f%%", peak2 * 100));

                audio1 = normalizeAudio(audio1, format, normalizeLevel);
                audio2 = normalizeAudio(audio2, format, normalizeLevel);

                printVerbose("Normalized to target level: " + String.format("%.1f%%", normalizeLevel * 100));
            }

            if (fadeDurationSeconds > 0 && fadeLengthBytes > 0) {
                // Apply crossfade
                int fadeStart = Math.max(0, audio1.length - fadeLengthBytes);

                // Write first file up to fade point
                outputBuffer.write(audio1, 0, fadeStart);

                // Extract fade regions
                int actualFadeLength = Math.min(fadeLengthBytes, audio1.length - fadeStart);
                actualFadeLength = Math.min(actualFadeLength, audio2.length);

                byte[] fadeRegion1 = new byte[actualFadeLength];
                byte[] fadeRegion2 = new byte[actualFadeLength];
                System.arraycopy(audio1, fadeStart, fadeRegion1, 0, actualFadeLength);
                System.arraycopy(audio2, 0, fadeRegion2, 0, actualFadeLength);

                // Apply crossfade and write
                byte[] crossfaded = applyCrossfade(fadeRegion1, fadeRegion2, format);
                outputBuffer.write(crossfaded);

                // Write remaining part of second file
                outputBuffer.write(audio2, actualFadeLength, audio2.length - actualFadeLength);

                if (fadeDurationSeconds > 0) {
                    printVerbose("Applied " + fadeDurationSeconds + "s crossfade between files");
                }
            } else {
                // No crossfade, simple concatenation
                outputBuffer.write(audio1);
                outputBuffer.write(audio2);
            }

            // Create output audio stream and write file
            byte[] finalAudio = outputBuffer.toByteArray();
            ByteArrayInputStream finalStream = new ByteArrayInputStream(finalAudio);
            long frameLength = finalAudio.length / format.getFrameSize();
            AudioInputStream finalAudioStream = new AudioInputStream(finalStream, format, frameLength);

            // Atomic file writing: write to temp file, then rename
            File outputFileObj = new File(outputFile);
            File tempFile = new File(outputFile + ".tmp");

            try {
                // Write to temporary file
                printVerbose("Writing to temporary file: " + tempFile.getPath());
                AudioSystem.write(finalAudioStream, AudioFileFormat.Type.WAVE, tempFile);
                finalAudioStream.close();

                // Atomic rename (moves temp file to final destination)
                printVerbose("Atomically renaming to: " + outputFileObj.getPath());
                if (outputFileObj.exists()) {
                    outputFileObj.delete(); // Delete existing file first (for Windows compatibility)
                }
                if (!tempFile.renameTo(outputFileObj)) {
                    throw new IOException("Failed to rename temporary file to output file");
                }

                printInfo("DJ Sacabambaspis has successfully made your sound lofi: " + outputFile);
                return true;
            } catch (IOException e) {
                // Clean up temp file on failure
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                throw e;
            }

        } catch (UnsupportedAudioFileException e) {
            System.err.println("error: unsupported audio file format");
            System.err.println("  " + e.getMessage());
            System.err.println("suggestion: ensure files are in WAV format");
            return false;
        } catch (IOException e) {
            System.err.println("error: I/O operation failed");
            System.err.println("  " + e.getMessage());
            System.err.println("suggestion: check file permissions and disk space");
            return false;
        } finally {
            // Clean up resources
            try {
                if (audioStream1 != null) audioStream1.close();
                if (audioStream2 != null) audioStream2.close();
                outputBuffer.close();
            } catch (IOException e) {
                System.err.println("warning: error closing streams - " + e.getMessage());
            }
        }
    }

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
        java.util.ArrayList<String> batchFiles = new java.util.ArrayList<>();

        // Parse flags and file arguments
        int fileArgCount = 0;
        java.util.ArrayList<String> fileArgsList = new java.util.ArrayList<>();

        for (String arg : args) {
            if ("--force".equals(arg)) {
                forceOverwrite = true;
            } else if ("--reverse".equals(arg)) {
                reverseMode = true;
            } else if ("-v".equals(arg) || "--verbose".equals(arg)) {
                verbosity = 2;
            } else if ("-q".equals(arg) || "--quiet".equals(arg)) {
                verbosity = 0;
            } else if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else if ("--shuffle".equals(arg)) {
                shuffleMode = true;
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
                printVerbose("Shuffled file order for creative mixing");
            }

            // Create output directory if it doesn't exist
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                if (!outputDirFile.mkdirs()) {
                    System.err.println("error: could not create output directory: " + outputDir);
                    System.exit(1);
                }
            }

            printInfo("Batch processing " + fileArgsList.size() + " file(s)...");
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

                printInfo("\n[" + (successCount + failCount + 1) + "/" + fileArgsList.size() + "] Processing: " + inputFile);

                // Check if output exists
                if (new File(outFilePath).exists() && !forceOverwrite) {
                    System.err.println("  Skipping: output file already exists (use --force to overwrite)");
                    failCount++;
                    continue;
                }

                // Process file
                if (combineSoundFiles(DEFAULT_INPUT_FILE1, inputFile, outFilePath, fadeDuration, normalizeLevel, dryRun, previewDuration)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            printInfo("\n=== Batch processing complete ===");
            printInfo("  Successful: " + successCount);
            printInfo("  Failed: " + failCount);
            printInfo("  Total: " + fileArgsList.size());

            System.exit(failCount > 0 ? 1 : 0);
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
                inputFile2 = DEFAULT_INPUT_FILE1;
            } else {
                // Normal mode: ambient first, then user file
                inputFile1 = DEFAULT_INPUT_FILE1;
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
            System.err.println("\nOptional flags:");
            System.err.println("  --force            Overwrite output file if it already exists");
            System.err.println("  --fade=<seconds>   Apply crossfade between files (e.g., --fade=1.5)");
            System.err.println("  --level=<0.0-1.0>  Normalize audio to target level (default: 0.8)");
            System.err.println("  --no-normalize     Disable automatic volume normalization");
            System.err.println("  --reverse          Swap file order (beat after content, not before)");
            System.err.println("  -v, --verbose        Show detailed processing information");
            System.err.println("  -q, --quiet          Suppress all output except errors");
            System.err.println("  --dry-run            Show what would be done without processing");
            System.err.println("  --preview=<seconds>  Process only first N seconds (e.g., --preview=30)");
            System.err.println("  --shuffle            Randomize file order for creative mixing");
            System.err.println("  --batch              Enable batch processing mode");
            System.err.println("  --output-dir=DIR     Output directory for batch mode (default: ./)");
            System.exit(1);
        }

        // Check if output file exists and warn user
        File outputFileObj = new File(outputFile);
        if (outputFileObj.exists() && !forceOverwrite) {
            System.err.println("error: output file '" + outputFile + "' already exists");
            System.err.println("suggestion: use a different output filename, or use --force to overwrite");
            System.err.println("           example: java Main input.wav output.wav --force");
            System.exit(1);
        }

        if (combineSoundFiles(inputFile1, inputFile2, outputFile, fadeDuration, normalizeLevel, dryRun, previewDuration)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
