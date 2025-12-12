import java.io.*;

/**
 * AmbientSelector - Handles ambient file selection and listing.
 */
public class AmbientSelector {

    private static final String ASSET_DIR = "../asset/";
    private static final String DEFAULT_AMBIENT = "ambient.wav";
    private static final String DEFAULT_INPUT_FILE1 = ASSET_DIR + DEFAULT_AMBIENT;

    // Available ambient files (users can add more to asset/ directory)
    private static final String[] AMBIENT_FILES = {
        "ambient.wav",
        "ambient_vinyl.wav",
        "ambient_rain.wav",
        "ambient_cafe.wav",
        "ambient_night.wav"
    };

    private Logger logger;

    public AmbientSelector(Logger logger) {
        this.logger = logger;
    }

    /**
     * Selects an ambient file based on user preference.
     *
     * @param ambientChoice User's ambient choice (filename, "random", or null for default)
     * @return Full path to the selected ambient file
     */
    public String selectAmbientFile(String ambientChoice) {
        if (ambientChoice == null || ambientChoice.isEmpty()) {
            // Default ambient file
            return DEFAULT_INPUT_FILE1;
        }

        if ("random".equalsIgnoreCase(ambientChoice)) {
            // Select random ambient file from available ones
            java.util.ArrayList<String> availableFiles = new java.util.ArrayList<>();

            for (String ambientFile : AMBIENT_FILES) {
                File f = new File(ASSET_DIR + ambientFile);
                if (f.exists()) {
                    availableFiles.add(ASSET_DIR + ambientFile);
                }
            }

            if (availableFiles.isEmpty()) {
                logger.printVerbose("Warning: no ambient files found, using default");
                return DEFAULT_INPUT_FILE1;
            }

            int randomIndex = new java.util.Random().nextInt(availableFiles.size());
            String selected = availableFiles.get(randomIndex);
            logger.printVerbose("Randomly selected ambient: " + new File(selected).getName());
            return selected;
        }

        // Check if it's a known ambient name (without path)
        String ambientPath = ASSET_DIR + ambientChoice;
        File ambientFile = new File(ambientPath);
        if (ambientFile.exists()) {
            logger.printVerbose("Using ambient: " + ambientChoice);
            return ambientPath;
        }

        // Try with .wav extension if not provided
        if (!ambientChoice.endsWith(".wav")) {
            ambientPath = ASSET_DIR + ambientChoice + ".wav";
            ambientFile = new File(ambientPath);
            if (ambientFile.exists()) {
                logger.printVerbose("Using ambient: " + ambientChoice + ".wav");
                return ambientPath;
            }
        }

        // Not found in asset directory, assume it's a full path
        File customFile = new File(ambientChoice);
        if (customFile.exists()) {
            logger.printVerbose("Using custom ambient: " + ambientChoice);
            return ambientChoice;
        }

        // Fallback to default
        System.err.println("warning: ambient file '" + ambientChoice + "' not found, using default");
        return DEFAULT_INPUT_FILE1;
    }

    /**
     * Lists available ambient files in the asset directory.
     */
    public void listAmbientFiles() {
        logger.printInfo("Available ambient files:");
        for (String ambientFile : AMBIENT_FILES) {
            File f = new File(ASSET_DIR + ambientFile);
            if (f.exists()) {
                logger.printInfo("  - " + ambientFile.replace(".wav", ""));
            }
        }
        logger.printInfo("  - random (selects randomly from available files)");
        logger.printInfo("  - Or provide a custom file path");
    }
}
