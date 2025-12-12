import javax.sound.sampled.*;
import java.io.*;

/**
 * AudioCombiner - Core audio combination logic.
 * Combines two audio files into one output file.
 */
public class AudioCombiner {

    private Logger logger;

    public AudioCombiner(Logger logger) {
        this.logger = logger;
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
     * @param loopCount Number of times to loop the first file (1 = no loop, 2 = double, etc.)
     * @return true if successful, false otherwise
     */
    public boolean combineSoundFiles(String inputFile1, String inputFile2, String outputFile, double fadeDurationSeconds, double normalizeLevel, boolean dryRun, double previewDuration, int loopCount) {
        // Validate input files
        if (!FileValidator.validateInputFile(inputFile1)) {
            return false;
        }
        if (!FileValidator.validateInputFile(inputFile2)) {
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

                logger.printInfo("=== DRY RUN MODE ===");
                logger.printInfo("\nInput File 1: " + inputFile1);
                logger.printInfo("  Size: " + (file1.length() / 1024) + " KB");
                logger.printInfo("  Duration: " + String.format("%.2f", stream1.getFrameLength() / fmt1.getFrameRate()) + " seconds");
                logger.printInfo("  Sample Rate: " + (int)fmt1.getSampleRate() + " Hz");
                logger.printInfo("  Channels: " + fmt1.getChannels());
                logger.printInfo("  Bit Depth: " + fmt1.getSampleSizeInBits() + " bits");

                logger.printInfo("\nInput File 2: " + inputFile2);
                logger.printInfo("  Size: " + (file2.length() / 1024) + " KB");
                logger.printInfo("  Duration: " + String.format("%.2f", stream2.getFrameLength() / fmt2.getFrameRate()) + " seconds");
                logger.printInfo("  Sample Rate: " + (int)fmt2.getSampleRate() + " Hz");
                logger.printInfo("  Channels: " + fmt2.getChannels());
                logger.printInfo("  Bit Depth: " + fmt2.getSampleSizeInBits() + " bits");

                long estimatedSize = file1.length() + file2.length();
                logger.printInfo("\nOutput File: " + outputFile);
                logger.printInfo("  Estimated Size: " + (estimatedSize / 1024) + " KB");
                logger.printInfo("  Estimated Duration: " + String.format("%.2f",
                    (stream1.getFrameLength() + stream2.getFrameLength()) / fmt1.getFrameRate()) + " seconds");

                logger.printInfo("\nSettings:");
                logger.printInfo("  Crossfade: " + (fadeDurationSeconds > 0 ? fadeDurationSeconds + " seconds" : "disabled"));
                logger.printInfo("  Normalization: " + (normalizeLevel > 0 ? String.format("%.1f%%", normalizeLevel * 100) : "disabled"));

                stream1.close();
                stream2.close();
                logger.printInfo("\nNo files were modified (dry run).");
                return true;
            } catch (Exception e) {
                logger.printError("error: could not read file metadata for dry run");
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

            if (!FileValidator.checkDiskSpace(outputFile, estimatedOutputSize)) {
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
                logger.printVerbose("Preview mode: limiting to " + previewDuration + " seconds per file");
                logger.printVerbose("  File 1: " + maxFrames1 + " frames");
                logger.printVerbose("  File 2: " + maxFrames2 + " frames");
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
                    logger.printProgress(bytesReadTotal1, Math.min(maxBytes1, file1ActualSize), "Reading file 1");
                }
            }
            if (showProgress1) {
                logger.printProgress(bytesReadTotal1, Math.min(maxBytes1, file1ActualSize), "Reading file 1");
            }
            byte[] audio1 = buffer1.toByteArray();

            // Apply looping if requested
            if (loopCount > 1) {
                logger.printVerbose("Looping first file " + loopCount + " times");
                audio1 = AudioProcessor.loopAudio(audio1, loopCount);
            }

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
                    logger.printProgress(bytesReadTotal2, Math.min(maxBytes2, file2ActualSize), "Reading file 2");
                }
            }
            if (showProgress2) {
                logger.printProgress(bytesReadTotal2, Math.min(maxBytes2, file2ActualSize), "Reading file 2");
            }
            byte[] audio2 = buffer2.toByteArray();

            if (previewDuration > 0) {
                logger.printInfo("Preview mode: processed " + previewDuration + " seconds from each file");
            }

            // Apply normalization if enabled
            if (normalizeLevel > 0) {
                double peak1 = AudioProcessor.findPeakLevel(audio1, format);
                double peak2 = AudioProcessor.findPeakLevel(audio2, format);

                logger.printVerbose("Pre-normalization levels:");
                logger.printVerbose("  File 1 peak: " + String.format("%.1f%%", peak1 * 100));
                logger.printVerbose("  File 2 peak: " + String.format("%.1f%%", peak2 * 100));

                audio1 = AudioProcessor.normalizeAudio(audio1, format, normalizeLevel);
                audio2 = AudioProcessor.normalizeAudio(audio2, format, normalizeLevel);

                logger.printVerbose("Normalized to target level: " + String.format("%.1f%%", normalizeLevel * 100));
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
                byte[] crossfaded = AudioProcessor.applyCrossfade(fadeRegion1, fadeRegion2, format);
                outputBuffer.write(crossfaded);

                // Write remaining part of second file
                outputBuffer.write(audio2, actualFadeLength, audio2.length - actualFadeLength);

                if (fadeDurationSeconds > 0) {
                    logger.printVerbose("Applied " + fadeDurationSeconds + "s crossfade between files");
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
                logger.printVerbose("Writing to temporary file: " + tempFile.getPath());
                AudioSystem.write(finalAudioStream, AudioFileFormat.Type.WAVE, tempFile);
                finalAudioStream.close();

                // Atomic rename (moves temp file to final destination)
                logger.printVerbose("Atomically renaming to: " + outputFileObj.getPath());
                if (outputFileObj.exists()) {
                    outputFileObj.delete(); // Delete existing file first (for Windows compatibility)
                }
                if (!tempFile.renameTo(outputFileObj)) {
                    throw new IOException("Failed to rename temporary file to output file");
                }

                logger.printInfo("DJ Sacabambaspis has successfully made your sound lofi: " + outputFile);
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
}
