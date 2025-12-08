![](https://img.shields.io/badge/lohigh_1.0-passing-light_green)
![](https://img.shields.io/badge/lohigh_2.0-passing-green)
![](https://img.shields.io/badge/lohigh_3.0-java-orange)

# `lohigh`

DJ Sacabambaspis lets you take lofi on the go.

![](asset/fish.jpg)

## features

✨ **Professional Audio Quality**
- Crossfade support for smooth transitions (eliminates clicks/pops)
- Automatic volume normalization for consistent output levels
- Comprehensive input validation and error handling

🚀 **Workflow Efficiency**
- Batch processing mode for multiple files
- Safe overwrite protection
- Helpful error messages with actionable suggestions

🎵 **Simple & Powerful**
- Single-purpose design with professional polish
- No external dependencies (pure Java)
- Cross-platform compatibility

## installation

```console
$ git clone https://github.com/gongahkia/lohigh
$ cd lohigh
$ make config
```

## usage

### Basic Usage

```console
$ make build
$ java -cp src Main input.wav output.wav
# DJ Sacabambaspis mixes up a lofi beat with ambient.wav

$ java -cp src Main input1.wav input2.wav output.wav
# You are the DJ - mix any two files together
```

### Advanced Features

**Crossfade** - Smooth transitions between audio files:
```console
$ java -cp src Main input.wav output.wav --fade=1.5
# Apply 1.5 second crossfade
```

**Volume Normalization** - Consistent output levels:
```console
$ java -cp src Main input.wav output.wav --level=0.8
# Normalize to 80% of maximum volume (default)

$ java -cp src Main input.wav output.wav --no-normalize
# Disable normalization
```

**Batch Processing** - Process multiple files efficiently:
```console
$ java -cp src Main --batch song1.wav song2.wav song3.wav
# Process multiple files with default ambient

$ java -cp src Main --batch *.wav --output-dir=./lofi_mixes/
# Process all WAV files in current directory

$ java -cp src Main --batch track*.wav --fade=1.5 --level=0.9 --force
# Batch process with custom settings
```

**Combine Multiple Features**:
```console
$ java -cp src Main input.wav output.wav --fade=2.0 --level=0.85 --force
# Crossfade + custom normalization + overwrite existing file
```

## command line options

| Flag | Description | Example |
|------|-------------|---------|
| `--fade=<seconds>` | Apply crossfade between files | `--fade=1.5` |
| `--level=<0.0-1.0>` | Normalize audio to target level | `--level=0.8` |
| `--no-normalize` | Disable automatic normalization | `--no-normalize` |
| `--force` | Overwrite existing output files | `--force` |
| `--batch` | Enable batch processing mode | `--batch` |
| `--output-dir=<dir>` | Output directory for batch mode | `--output-dir=./mixed/` |

## requirements

- Java 8 or higher
- No external dependencies (uses standard `javax.sound.sampled` library)

## what's new in v3.0

🎉 **Migrated from C++ to Java**
- Cross-platform compatibility
- No external dependencies (removed libsndfile requirement)
- Easier installation and distribution

🎚️ **Professional Audio Features**
- Crossfade support for click-free transitions
- Volume normalization for consistent output
- Enhanced input validation and error handling

⚡ **Workflow Improvements**
- Batch processing mode for multiple files
- Safe overwrite protection with --force flag
- Better error messages with helpful suggestions
