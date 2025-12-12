# lohigh Asset Directory

This directory contains ambient audio files used by lohigh for creating lofi mixes.

## Default Ambient File

- **ambient.wav** - The default lofi beat used when no specific ambient is specified

## Adding More Ambient Files

You can add your own ambient files to this directory for variety. lohigh supports the following ambient names out of the box:

- `ambient.wav` (default)
- `ambient_vinyl.wav` - Vinyl crackle and warmth
- `ambient_rain.wav` - Gentle rain sounds
- `ambient_cafe.wav` - Coffee shop ambience
- `ambient_night.wav` - Nighttime city sounds

### How to Add Custom Ambients

1. **Create or obtain a WAV file** with your desired ambient sound
2. **Name it appropriately** (e.g., `ambient_ocean.wav`)
3. **Copy it to this directory**
4. **Use it with lohigh**:
   ```bash
   java -cp src Main input.wav output.wav --ambient=ocean
   # or with full filename
   java -cp src Main input.wav output.wav --ambient=ambient_ocean.wav
   ```

### Ambient File Guidelines

For best results:
- **Format**: WAV (PCM)
- **Sample rate**: 44100 Hz recommended
- **Channels**: Stereo (2 channels) recommended
- **Bit depth**: 16-bit recommended
- **Length**: 30 seconds to 5 minutes works well
- **Volume**: Normalize to around -3dB to -6dB for good mix balance

### Using Random Ambients

To randomly select from available ambient files:

```bash
java -cp src Main input.wav output.wav --ambient=random
```

This will randomly choose from all ambient files found in this directory.

### Configuration File

You can set a default ambient in your `~/.lohighrc`:

```
# Use vinyl crackle by default
ambient=vinyl
```

### Listing Available Ambients

To see what ambient files are available:

```bash
java -cp src Main --list-ambients
```

## Tips

- Keep ambient files relatively short (1-3 minutes) to avoid bloating file sizes
- Use `--loop=N` to repeat short ambient loops multiple times
- Mix different ambients with `--ambient=random --shuffle` for variety
- Preview combinations with `--preview=30` before processing full files

---

DJ Sacabambaspis recommends experimenting with different ambient combinations to find your perfect lofi vibe! 🐟
