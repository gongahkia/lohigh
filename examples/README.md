# lohigh Examples

This directory contains examples demonstrating various ways to use lohigh for creating lofi music. These examples showcase the tool's features and inspire creative workflows.

## Table of Contents

1. [Basic Usage](#basic-usage)
2. [Audio Quality Features](#audio-quality-features)
3. [Batch Processing](#batch-processing)
4. [Creative Workflows](#creative-workflows)
5. [Advanced Techniques](#advanced-techniques)

---

## Basic Usage

### Example 1: Simple Lofi Conversion

Transform any audio file into a lofi track by mixing it with the default ambient sound.

```bash
# Mix your audio with the built-in ambient.wav
java -cp src Main podcast.wav podcast_lofi.wav

# Output: podcast_lofi.wav (podcast + ambient beats)
```

**Use Case**: Quick lofi conversion of podcasts, voice recordings, or simple tracks.

### Example 2: Combining Two Files

Mix any two audio files together.

```bash
# Combine a melody with a beat
java -cp src Main melody.wav beat.wav combined.wav

# Combine nature sounds with music
java -cp src Main rain.wav guitar.wav peaceful_music.wav
```

**Use Case**: Creating layered soundscapes, combining instruments, or mixing field recordings.

---

## Audio Quality Features

### Example 3: Smooth Crossfades

Eliminate clicks and pops at transitions with crossfade.

```bash
# Basic crossfade (1.5 seconds)
java -cp src Main intro.wav outro.wav smooth_mix.wav --fade=1.5

# Longer crossfade for very smooth transitions (3 seconds)
java -cp src Main ambient1.wav ambient2.wav seamless.wav --fade=3.0

# Quick fade (0.5 seconds)
java -cp src Main beat1.wav beat2.wav quick_transition.wav --fade=0.5
```

**Use Case**: Professional-sounding mixes, meditation tracks, continuous mixes.

### Example 4: Volume Normalization

Control output volume levels for consistent listening experience.

```bash
# Normalize to 80% (default, balanced level)
java -cp src Main quiet_file.wav loud_file.wav balanced.wav --level=0.8

# Normalize to 90% (louder output)
java -cp src Main file1.wav file2.wav louder.wav --level=0.9

# Normalize to 60% (softer, more subtle)
java -cp src Main file1.wav file2.wav gentle.wav --level=0.6

# Disable normalization (preserve original levels)
java -cp src Main file1.wav file2.wav raw.wav --no-normalize
```

**Use Case**: Creating consistent volume across multiple tracks, preventing distortion, matching loudness standards.

### Example 5: Combining Multiple Features

Use crossfade and normalization together for professional results.

```bash
# Professional mix with smooth transition and balanced volume
java -cp src Main track1.wav track2.wav output.wav --fade=2.0 --level=0.85

# Gentle mix with long fade and soft volume
java -cp src Main ambient1.wav ambient2.wav meditation.wav --fade=4.0 --level=0.6 --force
```

**Use Case**: Album mastering, podcast production, professional content creation.

---

## Batch Processing

### Example 6: Process Multiple Files

Transform an entire music library to lofi in one command.

```bash
# Process all WAV files in current directory
java -cp src Main --batch *.wav --output-dir=./lofi_versions/

# Process specific files
java -cp src Main --batch song1.wav song2.wav song3.wav --output-dir=./output/

# With custom settings
java -cp src Main --batch *.wav --output-dir=./lofi/ --fade=1.5 --level=0.85
```

**Output Files**: Creates `filename_lofi.wav` for each input file in the output directory.

**Use Case**: Processing music libraries, preparing podcast episodes, batch conversion.

### Example 7: Batch Processing with Overwrite

Handle existing output files in batch mode.

```bash
# Skip existing files (default)
java -cp src Main --batch *.wav --output-dir=./lofi/

# Force overwrite all files
java -cp src Main --batch *.wav --output-dir=./lofi/ --force
```

**Use Case**: Re-processing files with different settings, updating existing output.

### Example 8: Silent Batch Processing

Run batch jobs quietly for scripts and automation.

```bash
# Quiet mode - only show errors
java -cp src Main --batch *.wav --output-dir=./lofi/ -q

# Use in a shell script
#!/bin/bash
for dir in music/*; do
    java -cp src Main --batch "$dir"/*.wav --output-dir="$dir/lofi/" -q --force
done
echo "All directories processed!"
```

**Use Case**: Automated workflows, cron jobs, CI/CD pipelines.

---

## Creative Workflows

### Example 9: Reverse Mode

Place the ambient sound after your content instead of before.

```bash
# Normal mode: ambient THEN your audio
java -cp src Main spoken_word.wav output.wav

# Reverse mode: your audio THEN ambient
java -cp src Main spoken_word.wav output_reversed.wav --reverse
```

**Use Case**: Creating different moods, intro vs outro beats, experimenting with structure.

### Example 10: Shuffle Mode for Serendipitous Discovery

Randomize file order in batch mode for creative accidents.

```bash
# Random order creates unique combinations
java -cp src Main --batch track*.wav --output-dir=./random_mixes/ --shuffle

# Combine with other flags for creative exploration
java -cp src Main --batch *.wav --shuffle --fade=2.0 --output-dir=./experiments/
```

**Use Case**: Creative exploration, finding unexpected combinations, generative art.

### Example 11: Preview Mode for Experimentation

Test settings quickly without processing entire files.

```bash
# Preview first 30 seconds
java -cp src Main long_track.wav test.wav --preview=30

# Test different fade lengths quickly
java -cp src Main track.wav test1.wav --preview=15 --fade=0.5
java -cp src Main track.wav test2.wav --preview=15 --fade=1.5
java -cp src Main track.wav test3.wav --preview=15 --fade=3.0

# Preview batch processing
java -cp src Main --batch *.wav --output-dir=./tests/ --preview=10
```

**Use Case**: Finding the right settings, quick experimentation, testing workflows.

---

## Advanced Techniques

### Example 12: Dry Run Mode

Validate files and settings before committing to processing.

```bash
# Check file compatibility
java -cp src Main file1.wav file2.wav output.wav --dry-run

# See detailed file information
java -cp src Main input.wav output.wav --dry-run -v

# Verify batch operation
java -cp src Main --batch *.wav --output-dir=./lofi/ --dry-run
```

**Output Example**:
```
=== DRY RUN MODE ===

Input File 1: ambient.wav
  Size: 1024 KB
  Duration: 45.23 seconds
  Sample Rate: 44100 Hz
  Channels: 2
  Bit Depth: 16 bits

Input File 2: podcast.wav
  Size: 5120 KB
  Duration: 180.50 seconds
  Sample Rate: 44100 Hz
  Channels: 2
  Bit Depth: 16 bits

Output File: podcast_lofi.wav
  Estimated Size: 6144 KB
  Estimated Duration: 225.73 seconds

Settings:
  Crossfade: disabled
  Normalization: 80.0%

No files were modified (dry run).
```

**Use Case**: Validating format compatibility, estimating output size, verifying settings.

### Example 13: Verbose Mode for Debugging

See detailed processing information.

```bash
# Verbose output
java -cp src Main input.wav output.wav -v

# See normalization details
java -cp src Main quiet.wav loud.wav output.wav -v --level=0.8

# Debug batch processing
java -cp src Main --batch *.wav --output-dir=./lofi/ -v
```

**Output Example**:
```
[VERBOSE] Pre-normalization levels:
[VERBOSE]   File 1 peak: 45.2%
[VERBOSE]   File 2 peak: 78.9%
[VERBOSE] Normalized to target level: 80.0%
[VERBOSE] Applied 1.5s crossfade between files
[VERBOSE] Writing to temporary file: output.wav.tmp
[VERBOSE] Atomically renaming to: output.wav
DJ Sacabambaspis has successfully made your sound lofi: output.wav
```

**Use Case**: Troubleshooting, understanding processing, optimization.

### Example 14: Complex Production Workflow

Combine multiple features for a complete production pipeline.

```bash
# Professional podcast workflow
java -cp src Main intro.wav episode.wav temp1.wav --fade=1.0 --level=0.8
java -cp src Main temp1.wav outro.wav final_episode.wav --fade=1.0 --level=0.8 --force
rm temp1.wav

# Album mastering pipeline
for track in album/*.wav; do
    basename=$(basename "$track" .wav)
    java -cp src Main "$track" "mastered/${basename}_master.wav" \
        --fade=2.0 --level=0.85 -v
done

# Experimental music generation
java -cp src Main --batch samples/*.wav \
    --output-dir=./experiments/ \
    --shuffle --fade=3.0 --level=0.7 --force
```

**Use Case**: Professional production, album creation, automated workflows.

---

## Real-World Use Cases

### Music Production
```bash
# Create lofi hip-hop background music
java -cp src Main melody.wav beat.wav lofi_track.wav --fade=1.5 --level=0.8

# Layer field recordings with music
java -cp src Main rain_sounds.wav guitar.wav ambient_music.wav --fade=2.0
```

### Podcast Production
```bash
# Add intro music to podcast
java -cp src Main intro_music.wav podcast_content.wav final.wav --fade=1.0 --level=0.8

# Batch process entire season
java -cp src Main --batch season1/*.wav --output-dir=./season1_final/ --fade=1.0 --force
```

### Meditation & Relaxation
```bash
# Create long meditation tracks
java -cp src Main nature1.wav nature2.wav meditation.wav --fade=4.0 --level=0.6

# Preview for testing
java -cp src Main sounds1.wav sounds2.wav test.wav --preview=60 --fade=3.0
```

### Content Creation
```bash
# YouTube background music
java -cp src Main --batch music_loops/*.wav --output-dir=./yt_music/ --fade=2.0 --shuffle

# Game streaming ambient sounds
java -cp src Main ambient_loop.wav rain.wav stream_audio.wav --fade=5.0 --level=0.7
```

---

## Tips & Best Practices

### 1. File Format Compatibility
- Always use WAV files with matching sample rates for best results
- Convert other formats with ffmpeg first: `ffmpeg -i input.mp3 output.wav`
- Use `--dry-run -v` to verify compatibility before processing

### 2. Crossfade Duration
- **0.5-1s**: Quick transitions, energetic feel
- **1.5-2s**: Professional standard, smooth but noticeable
- **3-5s**: Very smooth, ambient and meditative
- **Experiment**: Use `--preview` mode to test different fade lengths quickly

### 3. Normalization Levels
- **0.6-0.7**: Gentle, background listening
- **0.8**: Default, balanced for most use cases
- **0.85-0.9**: Louder, attention-grabbing
- **Use --no-normalize**: When you want exact original levels preserved

### 4. Batch Processing Efficiency
- Use `-q` (quiet mode) for faster processing
- Process in smaller batches if memory is limited
- Use `--force` carefully to avoid accidentally overwriting important files

### 5. Experimentation
- Use `--preview=10` to quickly test settings on large files
- Try `--shuffle` mode for creative discoveries
- Combine `--dry-run -v` to understand what will happen before committing

---

## Need More Help?

- **README**: [../README.md](../README.md) - Complete feature documentation
- **Troubleshooting**: [../TROUBLESHOOTING.md](../TROUBLESHOOTING.md) - Common issues and solutions
- **Contributing**: [../CONTRIBUTING.md](../CONTRIBUTING.md) - Help improve lohigh

---

DJ Sacabambaspis hopes these examples inspire your lofi creations! 🐟
