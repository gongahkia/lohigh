/**
 * Logger - Handles all output and logging operations for lohigh.
 * Supports different verbosity levels and JSON output mode.
 */
public class Logger {

    // Verbosity levels
    private int verbosity = 1; // 0 = quiet, 1 = normal, 2 = verbose
    private boolean jsonOutput = false; // JSON output mode

    public Logger() {
        this(1, false);
    }

    public Logger(int verbosity, boolean jsonOutput) {
        this.verbosity = verbosity;
        this.jsonOutput = jsonOutput;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    public void setJsonOutput(boolean jsonOutput) {
        this.jsonOutput = jsonOutput;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    /**
     * Prints info message if verbosity level allows (unless in JSON mode).
     */
    public void printInfo(String message) {
        if (verbosity >= 1 && !jsonOutput) {
            System.out.println(message);
        }
    }

    /**
     * Prints verbose message if verbosity level allows (unless in JSON mode).
     */
    public void printVerbose(String message) {
        if (verbosity >= 2 && !jsonOutput) {
            System.out.println("[VERBOSE] " + message);
        }
    }

    /**
     * Prints error message (always shown unless in JSON mode).
     */
    public void printError(String message) {
        if (!jsonOutput) {
            System.err.println(message);
        }
    }

    /**
     * Escapes a string for JSON output.
     */
    private String escapeJson(String s) {
        if (s == null) return "null";
        return "\""  + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }

    /**
     * Outputs JSON result.
     */
    public void outputJson(boolean success, String outputFile, String[] inputFiles, String errorMessage, java.util.Map<String, Object> extraData) {
        if (!jsonOutput) return;

        System.out.println("{");
        System.out.println("  \"status\": " + escapeJson(success ? "success" : "error") + ",");

        // Input files array
        System.out.print("  \"input_files\": [");
        if (inputFiles != null && inputFiles.length > 0) {
            for (int i = 0; i < inputFiles.length; i++) {
                System.out.print(escapeJson(inputFiles[i]));
                if (i < inputFiles.length - 1) System.out.print(", ");
            }
        }
        System.out.println("],");

        // Output file
        System.out.println("  \"output_file\": " + escapeJson(outputFile) + ",");

        // Error message (if any)
        if (errorMessage != null) {
            System.out.println("  \"error\": " + escapeJson(errorMessage) + ",");
        }

        // Extra data
        if (extraData != null && !extraData.isEmpty()) {
            for (java.util.Map.Entry<String, Object> entry : extraData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                System.out.print("  " + escapeJson(key) + ": ");
                if (value instanceof String) {
                    System.out.println(escapeJson((String) value) + ",");
                } else if (value instanceof Number) {
                    System.out.println(value + ",");
                } else if (value instanceof Boolean) {
                    System.out.println(value + ",");
                } else {
                    System.out.println(escapeJson(value.toString()) + ",");
                }
            }
        }

        // Timestamp
        System.out.println("  \"timestamp\": " + escapeJson(java.time.Instant.now().toString()));
        System.out.println("}");
    }

    /**
     * Prints a progress bar.
     *
     * @param current Current progress value
     * @param total Total value
     * @param operation Description of operation
     */
    public void printProgress(long current, long total, String operation) {
        if (verbosity < 1 || jsonOutput) return; // Don't show in quiet or JSON mode

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
}
