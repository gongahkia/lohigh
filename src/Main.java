import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

/**
 * lohigh - DJ Sacabambaspis lets you take lofi on the go.
 * Java implementation of the audio file combiner.
 */
public class Main {

    private static final String DEFAULT_INPUT_FILE1 = "../asset/ambient.wav";

    /**
     * Combines two sound files into one output file by concatenating them.
     *
     * @param inputFile1 Path to the first input WAV file
     * @param inputFile2 Path to the second input WAV file
     * @param outputFile Path to the output WAV file
     * @return true if successful, false otherwise
     */
    public static boolean combineSoundFiles(String inputFile1, String inputFile2, String outputFile) {
        AudioInputStream audioStream1 = null;
        AudioInputStream audioStream2 = null;
        AudioInputStream appendedStream = null;

        try {
            // Read first file
            File file1 = new File(inputFile1);
            if (!file1.exists()) {
                System.err.println("DJ Sacabambaspis can't open the input file named '" + inputFile1 + "': File not found");
                return false;
            }
            audioStream1 = AudioSystem.getAudioInputStream(file1);

            // Read second file
            File file2 = new File(inputFile2);
            if (!file2.exists()) {
                System.err.println("DJ Sacabambaspis can't open the input file named '" + inputFile2 + "': File not found");
                audioStream1.close();
                return false;
            }
            audioStream2 = AudioSystem.getAudioInputStream(file2);

            // Get audio format from first file (this will be the output format)
            AudioFormat format = audioStream1.getFormat();

            // Check if formats are compatible
            AudioFormat format2 = audioStream2.getFormat();
            if (!format.matches(format2)) {
                System.err.println("DJ Sacabambaspis says: Audio formats don't match!");
                System.err.println("  File 1: " + format);
                System.err.println("  File 2: " + format2);
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
            System.err.println("DJ Sacabambaspis says: Unsupported audio file format - " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("DJ Sacabambaspis encountered an I/O error: " + e.getMessage());
            return false;
        } finally {
            // Clean up resources
            try {
                if (appendedStream != null) appendedStream.close();
                if (audioStream1 != null) audioStream1.close();
                if (audioStream2 != null) audioStream2.close();
            } catch (IOException e) {
                System.err.println("Error closing streams: " + e.getMessage());
            }
        }
    }

    /**
     * Main entry point for the lohigh application.
     *
     * @param args Command line arguments
     *        - 2 args: <input_file2.wav> <output_file.wav> (uses default ambient.wav as first file)
     *        - 3 args: <input_file1.wav> <input_file2.wav> <output_file.wav>
     */
    public static void main(String[] args) {
        String inputFile1 = null;
        String inputFile2 = null;
        String outputFile = null;

        if (args.length == 3) {
            // All arguments provided by user
            inputFile1 = args[0];
            inputFile2 = args[1];
            outputFile = args[2];
        } else if (args.length == 2) {
            // Defaults to specified lofi file
            inputFile1 = DEFAULT_INPUT_FILE1;
            inputFile2 = args[0];
            outputFile = args[1];
        } else {
            // Error: incorrect number of arguments
            System.err.println("DJ Sacabambaspis cannot make music because there are an incorrect number of files.");
            System.err.println("Provide either 2 or 3 arguments in the following format:");
            System.err.println("  java Main <input_file1.wav> <input_file2.wav> <output_file.wav>");
            System.err.println("  java Main <input_file2.wav> <output_file.wav>");
            System.exit(1);
        }

        if (combineSoundFiles(inputFile1, inputFile2, outputFile)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
