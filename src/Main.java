import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

/**
 * lohigh - DJ Sacabambaspis lets you take lofi on the go.
 * Java implementation of the audio file combiner.
 */
public class Main {

    private static final String DEFAULT_INPUT_FILE1 = "../asset/ambient.wav";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1GB default limit

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
     * Combines two sound files into one output file by concatenating them.
     *
     * @param inputFile1 Path to the first input WAV file
     * @param inputFile2 Path to the second input WAV file
     * @param outputFile Path to the output WAV file
     * @return true if successful, false otherwise
     */
    public static boolean combineSoundFiles(String inputFile1, String inputFile2, String outputFile) {
        // Validate input files
        if (!validateInputFile(inputFile1)) {
            return false;
        }
        if (!validateInputFile(inputFile2)) {
            return false;
        }

        AudioInputStream audioStream1 = null;
        AudioInputStream audioStream2 = null;
        AudioInputStream appendedStream = null;

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

            // Concatenate the audio streams
            appendedStream = new AudioInputStream(
                new SequenceInputStream(audioStream1, audioStream2),
                format,
                audioStream1.getFrameLength() + audioStream2.getFrameLength()
            );

            // Write to output file
            File outputFileObj = new File(outputFile);
            AudioSystem.write(appendedStream, AudioFileFormat.Type.WAVE, outputFileObj);

            System.out.println("DJ Sacabambaspis has successfully made your sound lofi: " + outputFile);
            return true;

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
                if (appendedStream != null) appendedStream.close();
                if (audioStream1 != null) audioStream1.close();
                if (audioStream2 != null) audioStream2.close();
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
     */
    public static void main(String[] args) {
        String inputFile1 = null;
        String inputFile2 = null;
        String outputFile = null;
        boolean forceOverwrite = false;

        // Check for --force flag
        int fileArgCount = 0;
        String[] fileArgs = new String[3];
        for (String arg : args) {
            if ("--force".equals(arg)) {
                forceOverwrite = true;
            } else {
                if (fileArgCount < 3) {
                    fileArgs[fileArgCount++] = arg;
                }
            }
        }

        if (fileArgCount == 3) {
            // All arguments provided by user
            inputFile1 = fileArgs[0];
            inputFile2 = fileArgs[1];
            outputFile = fileArgs[2];
        } else if (fileArgCount == 2) {
            // Defaults to specified lofi file
            inputFile1 = DEFAULT_INPUT_FILE1;
            inputFile2 = fileArgs[0];
            outputFile = fileArgs[1];
        } else {
            // Error: incorrect number of arguments
            System.err.println("DJ Sacabambaspis cannot make music because there are an incorrect number of files.");
            System.err.println("Provide either 2 or 3 arguments in the following format:");
            System.err.println("  java Main <input_file1.wav> <input_file2.wav> <output_file.wav>");
            System.err.println("  java Main <input_file2.wav> <output_file.wav>");
            System.err.println("\nOptional flags:");
            System.err.println("  --force    Overwrite output file if it already exists");
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

        if (combineSoundFiles(inputFile1, inputFile2, outputFile)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
