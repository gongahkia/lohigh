# Troubleshooting Guide

This guide covers common issues you might encounter when using lohigh and how to resolve them. DJ Sacabambaspis wants to help you get back to making lofi beats as quickly as possible!

## Table of Contents

- [Installation Issues](#installation-issues)
- [Audio Format Issues](#audio-format-issues)
- [File Permission Issues](#file-permission-issues)
- [Processing Issues](#processing-issues)
- [Output Issues](#output-issues)
- [Performance Issues](#performance-issues)
- [Getting More Help](#getting-more-help)

---

## Installation Issues

### "Java command not found" or "javac: command not found"

**Problem**: Java is not installed or not in your system PATH.

**Solution**:

1. **Check if Java is installed**:
   ```bash
   java -version
   javac -version
   ```

2. **Install Java** (if not installed):
   - **macOS**:
     ```bash
     brew install openjdk@11
     ```
     Or download from [Adoptium](https://adoptium.net/)

   - **Ubuntu/Debian**:
     ```bash
     sudo apt update
     sudo apt install openjdk-11-jdk
     ```

   - **Fedora/RHEL**:
     ```bash
     sudo dnf install java-11-openjdk-devel
     ```

   - **Windows**: Download from [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/)

3. **Verify installation**:
   ```bash
   make config
   ```

### "Build failed" or compilation errors

**Problem**: Source files may be corrupted or Java version incompatible.

**Solution**:

1. **Verify Java version** (need Java 8 or higher):
   ```bash
   java -version
   # Should show version 1.8 or higher
   ```

2. **Clean and rebuild**:
   ```bash
   rm -rf src/*.class
   make build
   ```

3. **Check for file corruption**:
   ```bash
   # Re-clone the repository if needed
   git status
   git restore src/Main.java  # If modified accidentally
   ```

---

## Audio Format Issues

### "error: unsupported audio file format"

**Problem**: The input file is not in a supported format or is corrupted.

**Solution**:

1. **Check file format**:
   ```bash
   file input.wav
   # Should show: RIFF (little-endian) data, WAVE audio
   ```

2. **Convert to WAV using ffmpeg**:
   ```bash
   # From MP3
   ffmpeg -i input.mp3 output.wav

   # From FLAC
   ffmpeg -i input.flac output.wav

   # From any format with specific settings
   ffmpeg -i input.m4a -ar 44100 -ac 2 output.wav
   ```

3. **Verify the converted file**:
   ```bash
   java -cp src Main output.wav test.wav --dry-run
   ```

### "error: audio format mismatch between input files"

**Problem**: The two input files have different sample rates, channels, or bit depths.

**Solution**:

1. **Check file details**:
   ```bash
   java -cp src Main file1.wav file2.wav output.wav --dry-run
   # This will show sample rates, channels, and bit depths
   ```

2. **Convert files to match**:
   ```bash
   # Match to first file's format (example: 44100 Hz, stereo)
   ffmpeg -i file2.wav -ar 44100 -ac 2 file2_converted.wav

   # Then process
   java -cp src Main file1.wav file2_converted.wav output.wav
   ```

3. **Common format conversions**:
   ```bash
   # Resample to 44.1kHz
   ffmpeg -i input.wav -ar 44100 output.wav

   # Convert to stereo
   ffmpeg -i input.wav -ac 2 output.wav

   # Convert to 16-bit
   ffmpeg -i input.wav -sample_fmt s16 output.wav

   # All at once
   ffmpeg -i input.wav -ar 44100 -ac 2 -sample_fmt s16 output.wav
   ```

### "error: file has invalid duration" or "file is empty"

**Problem**: The audio file has no frames or is corrupted.

**Solution**:

1. **Verify file integrity**:
   ```bash
   # Check file size
   ls -lh suspicious.wav

   # Try to play it
   afplay suspicious.wav  # macOS
   aplay suspicious.wav   # Linux
   ```

2. **Repair or re-export**:
   - If possible, re-export from the original source
   - Try repairing with ffmpeg:
     ```bash
     ffmpeg -i broken.wav -c copy fixed.wav
     ```

---

## File Permission Issues

### "error: cannot read 'file.wav' - permission denied"

**Problem**: The file doesn't have read permissions.

**Solution**:

```bash
# Check current permissions
ls -l file.wav

# Add read permissions
chmod +r file.wav

# Or give full user permissions
chmod u+rw file.wav
```

### "error: cannot write to output file"

**Problem**: No write permission in the output directory.

**Solution**:

```bash
# Check directory permissions
ls -ld output_directory/

# Add write permissions to directory
chmod u+w output_directory/

# Or try writing to a different location
java -cp src Main input.wav ~/Downloads/output.wav
```

---

## Processing Issues

### "error: insufficient disk space"

**Problem**: Not enough free disk space for the output file.

**Solution**:

1. **Check available space**:
   ```bash
   df -h .
   ```

2. **Free up space** or **use a different output directory**:
   ```bash
   # Use a different location with more space
   java -cp src Main input.wav /path/to/larger/drive/output.wav

   # Or in batch mode
   java -cp src Main --batch *.wav --output-dir=/path/to/larger/drive/
   ```

3. **Estimate output size**:
   ```bash
   # Output will be approximately the sum of input sizes
   ls -lh input1.wav input2.wav
   ```

### "error: file too large" (exceeds 1GB limit)

**Problem**: Input file exceeds the safety limit.

**Solution**:

1. **Split large files**:
   ```bash
   # Split into 10-minute chunks
   ffmpeg -i large.wav -f segment -segment_time 600 -c copy chunk%03d.wav
   ```

2. **Compress audio**:
   ```bash
   # Reduce to mono and lower sample rate
   ffmpeg -i large.wav -ac 1 -ar 22050 smaller.wav
   ```

3. **Process in preview mode** (for testing):
   ```bash
   java -cp src Main large.wav output.wav --preview=30
   ```

### Processing hangs or is very slow

**Problem**: Large files or slow I/O.

**Solution**:

1. **Use verbose mode to see progress**:
   ```bash
   java -cp src Main input.wav output.wav -v
   ```

2. **Progress indicators show automatically for files > 10MB**

3. **Test with preview mode first**:
   ```bash
   java -cp src Main input.wav test.wav --preview=10
   ```

4. **Check system resources**:
   ```bash
   # Monitor CPU and memory usage
   top
   # Press 'q' to quit
   ```

---

## Output Issues

### "error: output file 'output.wav' already exists"

**Problem**: Output file exists and safe overwrite protection is active.

**Solution**:

```bash
# Option 1: Use a different output filename
java -cp src Main input.wav output2.wav

# Option 2: Force overwrite
java -cp src Main input.wav output.wav --force

# Option 3: Delete the old file first
rm output.wav
java -cp src Main input.wav output.wav
```

### Output file sounds distorted or too quiet/loud

**Problem**: Volume levels need adjustment.

**Solution**:

1. **Adjust normalization level**:
   ```bash
   # Lower level (softer)
   java -cp src Main input.wav output.wav --level=0.6

   # Higher level (louder)
   java -cp src Main input.wav output.wav --level=0.95

   # Disable normalization entirely
   java -cp src Main input.wav output.wav --no-normalize
   ```

2. **Check source files**:
   ```bash
   # Dry run to see file properties
   java -cp src Main input1.wav input2.wav output.wav --dry-run -v
   ```

### Output file has clicks or pops at transitions

**Problem**: No crossfade applied, causing abrupt transitions.

**Solution**:

```bash
# Add crossfade (1-3 seconds usually works well)
java -cp src Main input.wav output.wav --fade=1.5

# Longer fade for smoother transitions
java -cp src Main input.wav output.wav --fade=3.0
```

### Batch mode skipping files

**Problem**: Output files already exist without --force flag.

**Solution**:

```bash
# Add --force to overwrite
java -cp src Main --batch *.wav --output-dir=./mixed/ --force

# Or clear the output directory first
rm -rf ./mixed/
mkdir ./mixed/
java -cp src Main --batch *.wav --output-dir=./mixed/
```

---

## Performance Issues

### Out of Memory errors

**Problem**: Very large files exhausting JVM heap.

**Solution**:

1. **Increase JVM heap size**:
   ```bash
   java -Xmx2g -cp src Main large1.wav large2.wav output.wav
   # -Xmx2g allocates 2GB of heap
   ```

2. **Process in smaller chunks**:
   ```bash
   # Use preview mode
   java -cp src Main input.wav output.wav --preview=60
   ```

3. **Reduce file sizes** before processing:
   ```bash
   # Downsample to 22kHz mono
   ffmpeg -i input.wav -ar 22050 -ac 1 smaller.wav
   ```

### Slow batch processing

**Problem**: Processing many files takes a long time.

**Solution**:

1. **Use quiet mode** to reduce I/O overhead:
   ```bash
   java -cp src Main --batch *.wav --output-dir=./mixed/ -q
   ```

2. **Process files in parallel** (manual):
   ```bash
   # In separate terminals or with & background jobs
   java -cp src Main file1.wav out1.wav &
   java -cp src Main file2.wav out2.wav &
   java -cp src Main file3.wav out3.wav &
   wait  # Wait for all to complete
   ```

---

## Getting More Help

If you've tried the solutions above and still have issues:

### 1. Enable Verbose Output

Run with verbose flag to see detailed information:
```bash
java -cp src Main input.wav output.wav -v
```

### 2. Check for Known Issues

Search the [GitHub issues](https://github.com/gongahkia/lohigh/issues) to see if others have encountered the same problem.

### 3. Gather Debug Information

Before reporting an issue, collect:
- Operating system and version
- Java version (`java -version`)
- Full command you ran
- Complete error output
- File details (if possible): format, size, sample rate

### 4. Report the Issue

Create a new issue on [GitHub](https://github.com/gongahkia/lohigh/issues/new) with:
- Clear title describing the problem
- Steps to reproduce
- Expected vs actual behavior
- Debug information collected above

### 5. Community Resources

- **README**: [README.md](README.md) - Full usage documentation
- **Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md) - Development guidelines
- **Changelog**: [CHANGELOG.md](CHANGELOG.md) - Version history

---

## Quick Reference: Common Commands

```bash
# Basic usage
java -cp src Main input.wav output.wav

# With crossfade and custom normalization
java -cp src Main input.wav output.wav --fade=1.5 --level=0.8

# Batch processing
java -cp src Main --batch *.wav --output-dir=./mixed/

# Preview first 30 seconds
java -cp src Main input.wav output.wav --preview=30

# Check files without processing
java -cp src Main input.wav output.wav --dry-run -v

# Silent mode for scripts
java -cp src Main input.wav output.wav -q

# Force overwrite existing files
java -cp src Main input.wav output.wav --force
```

---

DJ Sacabambaspis hopes this guide helps you troubleshoot any issues! If you discover solutions to new problems, consider contributing to this guide via a pull request. 🐟
