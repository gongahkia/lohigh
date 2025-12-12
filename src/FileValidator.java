import javax.sound.sampled.*;
import java.io.*;

/**
 * FileValidator - Handles file validation and disk space checking.
 */
public class FileValidator {

    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1GB default limit

    /**
     * Validates an input audio file for common issues.
     *
     * @param filePath Path to the audio file to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateInputFile(String filePath) {
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
    public static boolean checkDiskSpace(String outputPath, long estimatedSize) {
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
}
