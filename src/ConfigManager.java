import java.io.*;

/**
 * ConfigManager - Handles configuration file reading.
 * Reads configuration from ~/.lohighrc file.
 */
public class ConfigManager {

    private Logger logger;

    public ConfigManager(Logger logger) {
        this.logger = logger;
    }

    /**
     * Reads configuration from ~/.lohighrc file.
     * Returns a map of key-value pairs.
     */
    public java.util.Map<String, String> readConfigFile() {
        java.util.Map<String, String> config = new java.util.HashMap<>();

        // Check for config file in home directory
        String homeDir = System.getProperty("user.home");
        File configFile = new File(homeDir, ".lohighrc");

        if (!configFile.exists()) {
            return config; // Return empty config if file doesn't exist
        }

        logger.printVerbose("Reading configuration from " + configFile.getAbsolutePath());

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(configFile))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse key=value pairs
                int equalsIndex = line.indexOf('=');
                if (equalsIndex == -1) {
                    logger.printVerbose("Warning: invalid config line " + lineNum + ": " + line);
                    continue;
                }

                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                config.put(key, value);
                logger.printVerbose("Config: " + key + " = " + value);
            }
        } catch (IOException e) {
            logger.printVerbose("Warning: could not read config file: " + e.getMessage());
        }

        return config;
    }
}
