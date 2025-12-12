import java.io.*;

/**
 * IOHandler - Handles stdin/stdout operations for Unix pipeline support.
 */
public class IOHandler {

    /**
     * Checks if a file path represents stdin/stdout (i.e., "-").
     */
    public static boolean isStdio(String path) {
        return "-".equals(path);
    }

    /**
     * Reads audio from stdin into a temporary file.
     * Returns the path to the temporary file.
     */
    public static String readStdinToTempFile() throws IOException {
        File tempFile = File.createTempFile("lohigh_stdin_", ".wav");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = System.in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile.getAbsolutePath();
    }

    /**
     * Writes audio file to stdout.
     */
    public static void writeToStdout(String audioFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead);
            }
            System.out.flush();
        }
    }
}
