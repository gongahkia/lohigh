import java.io.*;

/**
 * PlaylistManager - Handles playlist file operations.
 * Supports plain text files (one path per line) and .m3u format.
 */
public class PlaylistManager {

    private Logger logger;

    public PlaylistManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * Reads a playlist file and returns a list of file paths.
     * Supports plain text files (one path per line) and .m3u format.
     *
     * @param playlistPath Path to the playlist file
     * @return ArrayList of file paths
     */
    public java.util.ArrayList<String> readPlaylist(String playlistPath) {
        java.util.ArrayList<String> files = new java.util.ArrayList<>();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(playlistPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and M3U comments (lines starting with #)
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                files.add(line);
            }
        } catch (IOException e) {
            logger.printError("error: could not read playlist file '" + playlistPath + "'");
            logger.printError("  " + e.getMessage());
            return null;
        }

        return files;
    }
}
