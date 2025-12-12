import javax.sound.sampled.*;

/**
 * AudioProcessor - Handles audio processing algorithms.
 * Includes normalization, crossfade, peak detection, and looping.
 */
public class AudioProcessor {

    /**
     * Finds the peak audio level in a byte array.
     *
     * @param audioData The audio data to analyze
     * @param format Audio format for sample interpretation
     * @return Peak level as a value between 0.0 and 1.0
     */
    public static double findPeakLevel(byte[] audioData, AudioFormat format) {
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
    public static byte[] normalizeAudio(byte[] audioData, AudioFormat format, double targetLevel) {
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
    public static byte[] applyCrossfade(byte[] fadeBuffer1, byte[] fadeBuffer2, AudioFormat format) {
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
     * Loops/repeats audio data N times by concatenating it with itself.
     *
     * @param audioData The audio data to loop
     * @param loopCount Number of times to loop (2 = double, 3 = triple, etc.)
     * @return Looped audio data
     */
    public static byte[] loopAudio(byte[] audioData, int loopCount) {
        if (loopCount <= 1) {
            return audioData;
        }

        byte[] result = new byte[audioData.length * loopCount];
        for (int i = 0; i < loopCount; i++) {
            System.arraycopy(audioData, 0, result, i * audioData.length, audioData.length);
        }
        return result;
    }
}
