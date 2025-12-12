# Changelog

All notable changes to lohigh will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Documentation Suite**:
  - CHANGELOG.md following Keep a Changelog format
  - CONTRIBUTING.md with community guidelines and philosophy
  - TROUBLESHOOTING.md with comprehensive problem-solving guide
  - examples/ directory with usage examples and creative workflows
  - Audio compatibility matrix in README
- **Distribution & Packaging**:
  - JAR packaging support with `make jar` command
  - Standalone JAR with embedded assets via `make jar-with-assets`
  - System-wide installation with `make install-jar`
- **Creative & Workflow Features**:
  - Playlist mode (--playlist=FILE) for sequential file mixing
  - Loop/repetition flag (--loop=N) to repeat files N times
  - JSON output mode (--json) for machine-readable results
- Integration support for scripts and automated workflows

## [3.0.0] - 2025-12-09

### Added
- **Migration to Java**: Complete rewrite from C++ to Java for cross-platform compatibility
- **Audio Quality Features**:
  - Crossfade support (--fade=duration) for smooth, click-free transitions between files
  - Volume normalization (--level=0.0-1.0) with automatic peak detection and scaling
  - --no-normalize flag to disable automatic normalization
- **Workflow & UX Features**:
  - Batch processing mode (--batch) for efficient multi-file operations
  - --output-dir flag for organizing batch output
  - Reverse mode (--reverse) to swap file order for creative flexibility
  - Progress indicators for files larger than 10MB
  - Preview mode (--preview=seconds) to test with first N seconds only
  - Dry run mode (--dry-run) for validation before processing
  - Verbose mode (-v, --verbose) for detailed processing information
  - Quiet mode (-q, --quiet) for silent operation in scripts
- **Creative Features**:
  - Shuffle mode (--shuffle) for randomized file ordering in batch mode
- **Reliability & Error Handling**:
  - Comprehensive input validation (file existence, readability, format validation)
  - Enhanced error messages with actionable suggestions
  - Safe overwrite protection with --force flag requirement
  - Atomic file writing using temporary files to prevent corruption
  - Disk space checking before processing
  - 1GB file size limit for safety
- **Developer Features**:
  - Debug build mode in Makefile (make debug)
  - Verbose compiler output option

### Changed
- Complete rewrite from C++ to Java
- Removed libsndfile dependency (now uses javax.sound.sampled)
- Default normalization level set to 0.8 (80% of maximum)
- Updated README with comprehensive feature documentation
- Improved command-line argument parsing

### Removed
- C++ implementation (migrated to Java)
- External library dependencies (now pure Java)

## [2.0.0] - 2024-XX-XX

### Added
- Enhanced lofi mixing capabilities
- Improved audio processing

### Changed
- Performance optimizations

## [1.0.0] - 2024-XX-XX

### Added
- Initial release of lohigh
- Basic WAV file combination
- Default ambient.wav mixing
- Simple command-line interface
- DJ Sacabambaspis personality

[Unreleased]: https://github.com/gongahkia/lohigh/compare/v3.0.0...HEAD
[3.0.0]: https://github.com/gongahkia/lohigh/releases/tag/3.0.0
[2.0.0]: https://github.com/gongahkia/lohigh/releases/tag/2.0.0
[1.0.0]: https://github.com/gongahkia/lohigh/releases/tag/1.0.0
