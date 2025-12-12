# Contributing to lohigh

Thank you for considering contributing to lohigh! DJ Sacabambaspis appreciates your help in making lofi music more accessible to everyone.

## Philosophy & Project Values

Before contributing, please understand lohigh's core philosophy:

### ✅ DO
- **Maintain radical simplicity**: Keep the tool focused on doing one thing well
- **Preserve personality**: DJ Sacabambaspis's playful tone should shine through
- **Default to sensible behaviors**: Features should work well without configuration
- **Add opt-in features**: New functionality should be flags, not required complexity
- **Write clear, readable code**: Favor clarity over cleverness
- **Test with real-world files**: Use actual music, podcasts, and field recordings
- **Document with examples**: Show, don't just tell

### ❌ DON'T
- **Add GUI or TUI interfaces**: Stay focused on CLI simplicity
- **Require configuration files** for basic operation (they're fine as optional)
- **Introduce heavyweight dependencies**: Keep the dependency footprint minimal
- **Create artificial limitations**: Don't add caps that hurt legitimate use
- **Over-engineer for hypothetical futures**: Solve today's problems today
- **Lose the fun**: Keep DJ Sacabambaspis's personality alive
- **Compete with DAWs**: We're a focused tool, not a full production suite

## How to Contribute

### Reporting Bugs

Before creating a bug report:
1. Check the [troubleshooting guide](TROUBLESHOOTING.md) for common issues
2. Search existing issues to avoid duplicates
3. Test with the latest version from `main` branch

When reporting a bug, include:
- **Operating system and version** (macOS, Linux, Windows with version)
- **Java version** (`java -version`)
- **Command you ran** (exact command with all flags)
- **Expected behavior** vs **actual behavior**
- **Error messages** (full output with `-v` verbose flag if possible)
- **Sample files** (if possible, or description of file format/size)

### Suggesting Features

We love feature suggestions! Before suggesting:
1. Review [claude_tasks.txt](claude_tasks.txt) to see if it's already planned
2. Check existing feature requests in issues
3. Consider if it aligns with lohigh's philosophy (see above)

When suggesting a feature:
- **Explain the use case**: Why is this valuable? What problem does it solve?
- **Describe expected behavior**: How should it work?
- **Consider simplicity**: How can this integrate with minimal complexity?
- **Provide examples**: Show command-line usage examples

### Contributing Code

#### Development Setup

1. **Fork and clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/lohigh.git
   cd lohigh
   ```

2. **Verify your environment**:
   ```bash
   make config
   # Should show Java 8 or higher
   ```

3. **Build the project**:
   ```bash
   make build
   ```

4. **Test your setup**:
   ```bash
   java -cp src Main asset/ambient.wav test_output.wav
   ```

#### Code Style Guidelines

**Java Conventions**:
- Use 4 spaces for indentation (no tabs)
- Use camelCase for variables and methods
- Use PascalCase for class names
- Follow standard Java naming conventions
- Maximum line length: 120 characters
- Add Javadoc comments for public methods

**Example**:
```java
/**
 * Combines two audio files with optional crossfade.
 *
 * @param file1 Path to first audio file
 * @param file2 Path to second audio file
 * @param fadeDuration Crossfade duration in seconds
 * @return true if successful, false otherwise
 */
public static boolean combineWithFade(String file1, String file2, double fadeDuration) {
    // Implementation here
}
```

**Error Handling**:
- Always provide helpful error messages with suggestions
- Use the established `printError()`, `printInfo()`, and `printVerbose()` functions
- Include context in error messages (file names, values, etc.)

**Example**:
```java
if (fileSize > MAX_FILE_SIZE) {
    System.err.println("error: '" + filePath + "' is too large (" + (fileSize / 1024 / 1024) + " MB)");
    System.err.println("suggestion: file exceeds maximum size of " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
    return false;
}
```

#### Making Changes

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**:
   - Keep commits focused and atomic
   - Write clear commit messages (see format below)
   - Test thoroughly with various file types and sizes

3. **Test your changes**:
   ```bash
   # Build
   make build

   # Test basic functionality
   java -cp src Main input1.wav input2.wav output.wav

   # Test with flags
   java -cp src Main input.wav output.wav --fade=1.5 --level=0.8

   # Test batch mode
   java -cp src Main --batch *.wav --output-dir=./test/

   # Test error cases (missing files, invalid formats, etc.)
   ```

4. **Commit format**:
   ```
   type(scope): brief description

   Longer description explaining:
   - What changed
   - Why it changed
   - Any breaking changes or important notes

   Closes #issue_number (if applicable)
   ```

   Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

   Example:
   ```
   feat(audio): add multi-ambient library support

   - Users can now choose from multiple ambient files
   - Added --ambient=vinyl|rain|cafe|random flag
   - Ships with 5 default ambient files in asset/ directory
   - Maintains backward compatibility with single ambient.wav

   Implements task 4.1 from claude_tasks.txt
   Closes #42
   ```

#### Pull Request Process

1. **Update documentation**:
   - Update README.md if adding user-facing features
   - Update CHANGELOG.md under [Unreleased] section
   - Add code comments where logic isn't self-evident

2. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request**:
   - Use a clear, descriptive title
   - Reference any related issues
   - Describe what changed and why
   - Include screenshots/examples if applicable
   - Note any breaking changes

4. **PR Review Process**:
   - Maintainers will review within a few days
   - Address any feedback or requested changes
   - Once approved, maintainers will merge

### Testing

Currently, lohigh doesn't have an automated test suite (contributions welcome!). Manual testing is crucial:

**Test Cases to Cover**:
- ✅ Basic two-file combination
- ✅ Single file with default ambient.wav
- ✅ Various file sizes (small, medium, large)
- ✅ Different sample rates (22050, 44100, 48000 Hz)
- ✅ Different bit depths (16-bit, 24-bit if supported)
- ✅ Mono and stereo files
- ✅ Edge cases: empty files, very short files, corrupted files
- ✅ All command-line flags individually and in combination
- ✅ Batch processing with multiple files
- ✅ Error handling (missing files, permission errors, disk space)

## Community

### Code of Conduct

- **Be respectful**: Treat all contributors with kindness and respect
- **Be constructive**: Provide helpful feedback and suggestions
- **Be collaborative**: Work together to make lohigh better
- **Be patient**: Everyone's learning and growing
- **Have fun**: DJ Sacabambaspis is all about the vibes

### Questions?

- Check the [README](README.md) for usage documentation
- Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues
- Search existing [GitHub issues](https://github.com/gongahkia/lohigh/issues)
- Open a new issue with the "question" label

### Recognition

All contributors will be recognized in:
- Git commit history (use `Co-Authored-By` for collaborations)
- Release notes for significant contributions
- The project's gratitude and appreciation

## License

By contributing to lohigh, you agree that your contributions will be licensed under the same license as the project (check LICENSE file).

---

Thank you for contributing to lohigh! DJ Sacabambaspis is grateful for your help in spreading lofi vibes to the world. 🐟
