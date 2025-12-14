[![](https://img.shields.io/badge/lohigh_1.0-passing-light_green)](https://github.com/gongahkia/lohigh/releases/tag/1.0)
[![](https://img.shields.io/badge/lohigh_2.0-passing-green)](https://github.com/gongahkia/lohigh/releases/tag/2.0)

# `lohigh`

DJ Sacabambaspis lets you take lofi on the go.

![](asset/fish.jpg)

## installation

Note that `lohigh` requires Java 8 or higher to run.

```console
$ git clone https://github.com/gongahkia/lohigh
$ cd lohigh
$ make config
$ make build
```

## usage

```console
$ java -cp src Main input.wav output.wav # DJ Sacabambaspis mixes up a lofi beat with ambient.wav
$ java -cp src Main input1.wav input2.wav output.wav # DJ Sacabambaspis mixes any two files together
$ java -cp src Main input.wav output.wav --fade=1.5 # DJ Sacabambaspis applies a 1.5 second crossfade
$ java -cp src Main input.wav output.wav --level=0.8 # DJ Sacabambaspis normalizes the audio track to 80% of maximum volume 
$ java -cp src Main input.wav output.wav --no-normalize # DJ Sacabambaspis disables normalization 
```

## CLI options

### audio processing

| flag | description | eg. |
|------|-------------|---------|
| `--fade=<seconds>` | Apply crossfade between files | `--fade=1.5` |
| `--level=<0.0-1.0>` | Normalize audio to target level (default: 0.8) | `--level=0.9` |
| `--no-normalize` | Disable automatic normalization | `--no-normalize` |

### workflow & UX

| flag | description | eg. |
|------|-------------|---------|
| `--force` | Overwrite existing output files | `--force` |
| `--reverse` | Swap file order (beat after content) | `--reverse` |
| `-v`, `--verbose` | Show detailed processing information | `-v` |
| `-q`, `--quiet` | Suppress all output except errors | `-q` |
| `--dry-run` | Show what would be done without processing | `--dry-run` |
| `--preview=<seconds>` | Process only first N seconds | `--preview=30` |

### batch processing

| flag | description | eg. |
|------|-------------|---------|
| `--batch` | Enable batch processing mode | `--batch` |
| `--output-dir=<dir>` | Output directory for batch mode | `--output-dir=./mixed/` |
| `--shuffle` | Randomize file order for creative mixing | `--shuffle` |

## other notes

`lohigh` used to exist as a single-file C++ program, but has since been refactored to a Java project.