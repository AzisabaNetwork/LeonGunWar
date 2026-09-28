package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Color;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/** Operator-authored kill action definitions. Only valid entries are published on reload. */
public final class KillActionCatalog {
    public enum Category {
        SOUND("killsound"), LOG("killlog"), PARTICLE("killparticle");
        public final String folder;
        Category(String folder) { this.folder = folder; }
    }

    public enum Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, SPECIAL }

    public record SoundCue(Sound sound, float volume, float pitch, int tick) { }
    record SoundSpec(String name, float volume, float pitch, int tick) { }
    public record Point(double x, double y, double z) { }
    public record Frame(int tick, List<Point> points) { }
    public record ParticleEffect(Particle particle, Object data, String shape, boolean killerOrigin,
                                 int startTick, int durationTick, int intervalTick, int count,
                                 double radius, double height, double speed, List<Frame> frames) { }
    public record Entry(Category category, String id, String displayName, Material icon, List<String> lore,
                        Rarity rarity, String permission, String unlockDescription, String format,
                        List<SoundCue> sounds, List<ParticleEffect> effects) { }

    public static final Set<String> SHAPES = Set.of("burst", "ring", "sphere", "spiral", "double_helix",
            "heart", "star", "flower", "crown", "vortex", "fountain", "wave", "comet", "lightning", "cube", "points");

    private final LeonGunWar plugin;
    private final Set<String> registeredPermissions = new HashSet<>();
    private volatile Map<Category, Map<String, Entry>> entries = Map.of();
    private volatile int soundRadius = 32;
    private volatile int particleRadius = 48;
    private volatile int maxAnimationTicks = 100;
    private volatile int maxParticlesPerKill = 2000;

    public KillActionCatalog(LeonGunWar plugin) { this.plugin = plugin; }

    public Map<String, Entry> entries(Category category) { return entries.getOrDefault(category, Map.of()); }
    public Entry get(Category category, String id) { return id == null ? null : entries(category).get(id); }
    public int soundRadius() { return soundRadius; }
    public int particleRadius() { return particleRadius; }
    public int maxAnimationTicks() { return maxAnimationTicks; }
    public int maxParticlesPerKill() { return maxParticlesPerKill; }

    public synchronized void reload() throws IOException {
        File dataFolder = plugin.getDataFolder();
        Files.createDirectories(dataFolder.toPath());
        File settingsFile = new File(dataFolder, "killAction.yml");
        if (!settingsFile.isFile()) {
            Files.writeString(settingsFile.toPath(), "sound-radius: 32\nparticle-radius: 48\nmax-animation-ticks: 100\nmax-particles-per-kill: 2000\n", StandardCharsets.UTF_8);
        }
        YamlConfiguration settings = read(settingsFile);
        int newSoundRadius, newParticleRadius, newMaxTicks, newMaxParticles;
        try {
            newSoundRadius = bounded(settings.getInt("sound-radius", 32), 1, 128, "sound-radius");
            newParticleRadius = bounded(settings.getInt("particle-radius", 48), 1, 128, "particle-radius");
            newMaxTicks = bounded(settings.getInt("max-animation-ticks", 100), 1, 200, "max-animation-ticks");
            newMaxParticles = bounded(settings.getInt("max-particles-per-kill", 2000), 1, 10000, "max-particles-per-kill");
        } catch (IllegalArgumentException ex) { throw new IOException("Invalid killAction.yml: " + ex.getMessage(), ex); }

        Map<Category, Map<String, Entry>> loaded = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            File folder = new File(dataFolder, category.folder);
            boolean firstUse = !folder.exists();
            Files.createDirectories(folder.toPath());
            if (firstUse) {
                try (InputStream example = plugin.getResource(category.folder + "/example.yml")) {
                    if (example != null) Files.copy(example, new File(folder, "example.yml").toPath());
                }
            }
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
            if (files == null) throw new IOException("Cannot list " + folder);
            Arrays.sort(files, Comparator.comparing(File::getName));
            Map<String, Entry> categoryEntries = new LinkedHashMap<>();
            for (File file : files) {
                YamlConfiguration yaml;
                try { yaml = read(file); }
                catch (Exception ex) {
                    plugin.getLogger().warning("KillAction: " + file.getName() + ": " + ex.getMessage());
                    continue;
                }
                for (String id : yaml.getKeys(false)) {
                    try {
                        if (!id.matches("[A-Za-z0-9_-]{1,64}")) throw new IllegalArgumentException("invalid ID");
                        if (categoryEntries.containsKey(id)) throw new IllegalArgumentException("duplicate ID");
                        ConfigurationSection section = yaml.getConfigurationSection(id);
                        if (section == null) throw new IllegalArgumentException("entry must be a section");
                        categoryEntries.put(id, parse(category, id, section, newMaxTicks));
                    } catch (RuntimeException ex) {
                        plugin.getLogger().warning("KillAction: " + category.folder + "/" + file.getName()
                                + " [" + id + "]: " + ex.getMessage());
                    }
                }
            }
            loaded.put(category, Collections.unmodifiableMap(categoryEntries));
        }
        for (Map<String, Entry> categoryEntries : loaded.values()) {
            for (Entry entry : categoryEntries.values()) {
                if (Bukkit.getPluginManager().getPermission(entry.permission()) == null) {
                    Bukkit.getPluginManager().addPermission(new Permission(entry.permission(), PermissionDefault.FALSE));
                    registeredPermissions.add(entry.permission());
                }
            }
        }
        soundRadius = newSoundRadius;
        particleRadius = newParticleRadius;
        maxAnimationTicks = newMaxTicks;
        maxParticlesPerKill = newMaxParticles;
        entries = Collections.unmodifiableMap(loaded);
    }

    public void close() {
        for (String permission : registeredPermissions) Bukkit.getPluginManager().removePermission(permission);
        registeredPermissions.clear();
    }

    private static YamlConfiguration read(File file) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        try { yaml.loadFromString(Files.readString(file.toPath(), StandardCharsets.UTF_8)); }
        catch (org.bukkit.configuration.InvalidConfigurationException ex) { throw new IOException(ex.getMessage(), ex); }
        return yaml;
    }

    static Entry parse(Category category, String id, ConfigurationSection section, int maxTicks) {
        String displayName = required(section.getString("Displayname"), "Displayname");
        String iconName = required(section.getString("GUIicon"), "GUIicon");
        Material icon = Material.matchMaterial(iconName);
        if (icon == null || !icon.isItem()) throw new IllegalArgumentException("invalid GUIicon: " + iconName);
        String loreText = required(section.getString("lore"), "lore");
        List<String> lore = Arrays.stream(loreText.split("\\|", -1)).toList();
        Rarity rarity;
        try { rarity = Rarity.valueOf(required(section.getString("rarity"), "rarity").toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("invalid rarity", ex); }
        String permission = required(section.getString("Unlock.permission"), "Unlock.permission");
        String description = required(section.getString("Unlock.description"), "Unlock.description");
        String format = null;
        List<SoundCue> sounds = List.of();
        List<ParticleEffect> effects = List.of();
        switch (category) {
            case LOG -> format = required(section.getString("Format"), "Format");
            case SOUND -> sounds = parseSounds(required(section.getString("Sounds"), "Sounds"), maxTicks);
            case PARTICLE -> effects = parseEffects(section.getMapList("Effects"), maxTicks);
        }
        return new Entry(category, id, displayName, icon, lore, rarity, permission, description,
                format, sounds, effects);
    }

    static List<SoundCue> parseSounds(String input, int maxTicks) {
        List<SoundCue> result = new ArrayList<>();
        for (SoundSpec spec : parseSoundSpecs(input, maxTicks)) {
            try {
                Sound sound = (Sound) Sound.class.getField(spec.name()).get(null);
                result.add(new SoundCue(sound, spec.volume(), spec.pitch(), spec.tick()));
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException("unknown Sound: " + spec.name(), ex);
            }
        }
        return List.copyOf(result);
    }

    static List<SoundSpec> parseSoundSpecs(String input, int maxTicks) {
        String[] chunks = input.split(",", -1);
        if (chunks.length > 16) throw new IllegalArgumentException("too many Sounds (max 16)");
        List<SoundSpec> result = new ArrayList<>();
        for (String chunk : chunks) {
            String[] parts = chunk.trim().split("-", -1);
            if (parts.length != 4) throw new IllegalArgumentException("Sounds must use NAME-v-p-t");
            try {
                String name = required(parts[0], "Sound name").toUpperCase(Locale.ROOT);
                float volume = Float.parseFloat(parts[1]);
                float pitch = Float.parseFloat(parts[2]);
                int tick = Integer.parseInt(parts[3]);
                if (!Float.isFinite(volume) || volume < 0 || volume > 4
                        || !Float.isFinite(pitch) || pitch < .5f || pitch > 2f
                        || tick < 0 || tick > maxTicks) throw new IllegalArgumentException("invalid v, p or t");
                result.add(new SoundSpec(name, volume, pitch, tick));
            } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("invalid Sounds entry: " + chunk, ex); }
        }
        return List.copyOf(result);
    }

    private static List<ParticleEffect> parseEffects(List<Map<?, ?>> maps, int maxTicks) {
        if (maps.isEmpty() || maps.size() > 16) throw new IllegalArgumentException("Effects must have 1-16 entries");
        List<ParticleEffect> effects = new ArrayList<>();
        for (Map<?, ?> map : maps) {
            String particleName = required(string(map, "Particle", null), "Effects.Particle");
            Particle particle;
            try { particle = Particle.valueOf(particleName.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex) { throw new IllegalArgumentException("invalid Particle: " + particleName, ex); }
            String shape = required(string(map, "shape", null), "Effects.shape").toLowerCase(Locale.ROOT);
            if (!SHAPES.contains(shape)) throw new IllegalArgumentException("invalid shape: " + shape);
            String origin = string(map, "origin", "victim").toLowerCase(Locale.ROOT);
            if (!origin.equals("victim") && !origin.equals("killer")) throw new IllegalArgumentException("invalid origin");
            int start = integer(map, "startTick", 0, 0, maxTicks);
            int duration = integer(map, "durationTick", 1, 1, maxTicks);
            int interval = integer(map, "intervalTick", 1, 1, maxTicks);
            int count = integer(map, "count", 32, 1, 200);
            double radius = number(map, "radius", 1.5, 0, 8);
            double height = number(map, "height", 2, 0, 8);
            double speed = number(map, "speed", 0, 0, 2);
            if (start + duration - 1 > maxTicks) throw new IllegalArgumentException("effect exceeds max-animation-ticks");
            Object data = parseParticleData(particle, map);
            List<Frame> frames = shape.equals("points") ? parseFrames(map, maxTicks) : List.of();
            effects.add(new ParticleEffect(particle, data, shape, origin.equals("killer"), start,
                    duration, interval, count, radius, height, speed, frames));
        }
        return List.copyOf(effects);
    }

    private static Object parseParticleData(Particle particle, Map<?, ?> map) {
        Class<?> type = particle.getDataType();
        if (type == Void.class) return null;
        if (type == Particle.DustOptions.class) {
            Color color = color(string(map, "color", "#FFFFFF"));
            return new Particle.DustOptions(color, (float) number(map, "size", 1, .1, 4));
        }
        if (type == Particle.DustTransition.class) {
            return new Particle.DustTransition(color(string(map, "color", "#FFFFFF")),
                    color(string(map, "toColor", "#FFFFFF")), (float) number(map, "size", 1, .1, 4));
        }
        if (type == Color.class) return color(string(map, "color", "#FFFFFF"));
        if (org.bukkit.block.data.BlockData.class.isAssignableFrom(type)) {
            Material material = Material.matchMaterial(required(string(map, "blockMaterial", null), "blockMaterial"));
            if (material == null || !material.isBlock()) throw new IllegalArgumentException("invalid blockMaterial");
            return material.createBlockData();
        }
        if (ItemStack.class.isAssignableFrom(type)) {
            Material material = Material.matchMaterial(required(string(map, "itemMaterial", null), "itemMaterial"));
            if (material == null || !material.isItem()) throw new IllegalArgumentException("invalid itemMaterial");
            return new ItemStack(material);
        }
        throw new IllegalArgumentException("unsupported data for Particle " + particle.name());
    }

    static List<Frame> parseFrames(Map<?, ?> map, int maxTicks) {
        Object raw = map.get("frames");
        if (!(raw instanceof List<?> list) || list.isEmpty()) throw new IllegalArgumentException("points shape requires frames");
        List<Frame> frames = new ArrayList<>();
        int points = 0;
        for (Object frameValue : list) {
            if (!(frameValue instanceof Map<?, ?> frame)) throw new IllegalArgumentException("invalid frame");
            int tick = integer(frame, "tick", -1, 0, maxTicks);
            Object rawPoints = frame.get("points");
            if (!(rawPoints instanceof List<?> values) || values.isEmpty()) throw new IllegalArgumentException("frame needs points");
            List<Point> coordinates = new ArrayList<>();
            for (Object value : values) {
                if (!(value instanceof List<?> xyz) || xyz.size() != 3) throw new IllegalArgumentException("point must be [x,y,z]");
                coordinates.add(new Point(coordinate(xyz.get(0)), coordinate(xyz.get(1)), coordinate(xyz.get(2))));
            }
            points += coordinates.size();
            if (points > 2000) throw new IllegalArgumentException("too many custom points");
            frames.add(new Frame(tick, List.copyOf(coordinates)));
        }
        return List.copyOf(frames);
    }

    private static double coordinate(Object raw) {
        if (!(raw instanceof Number number) || !Double.isFinite(number.doubleValue()) || Math.abs(number.doubleValue()) > 16)
            throw new IllegalArgumentException("point coordinate must be within -16..16");
        return number.doubleValue();
    }

    private static Color color(String hex) {
        if (!hex.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("color must be #RRGGBB");
        return Color.fromRGB(Integer.parseInt(hex.substring(1), 16));
    }

    private static String string(Map<?, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : value.toString();
    }
    private static int integer(Map<?, ?> map, String key, int defaultValue, int min, int max) {
        Object value = map.get(key);
        if (value == null) return bounded(defaultValue, min, max, key);
        if (!(value instanceof Number number) || number.doubleValue() != Math.rint(number.doubleValue()))
            throw new IllegalArgumentException(key + " must be an integer");
        return bounded(number.intValue(), min, max, key);
    }
    private static double number(Map<?, ?> map, String key, double defaultValue, double min, double max) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())
                || number.doubleValue() < min || number.doubleValue() > max)
            throw new IllegalArgumentException(key + " must be within " + min + ".." + max);
        return number.doubleValue();
    }
    private static int bounded(int value, int min, int max, String key) {
        if (value < min || value > max) throw new IllegalArgumentException(key + " must be within " + min + ".." + max);
        return value;
    }
    private static String required(String value, String key) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }
}
