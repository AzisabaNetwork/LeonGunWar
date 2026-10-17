package net.azisaba.lgw.core.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

/** Atomic UTF-8 save without replacing existing POSIX access settings with temp-file defaults. */
public final class AtomicConfigWriter {
    private AtomicConfigWriter() {}

    public static void write(Path file, String prefix, String content) throws IOException {
        Path target = Files.exists(file) ? file.toRealPath() : file.toAbsolutePath();
        Files.createDirectories(target.getParent());
        var directoryView = Files.getFileAttributeView(target.getParent(), PosixFileAttributeView.class);
        var existingView = Files.exists(target) ? Files.getFileAttributeView(target, PosixFileAttributeView.class) : null;
        PosixFileAttributes original = existingView == null ? null : existingView.readAttributes();
        // New config files use ordinary rw creation permissions, filtered by the OS umask.
        // Existing files retain even deliberately restricted permissions such as 0600.
        Path temporary = directoryView == null
                ? Files.createTempFile(target.getParent(), prefix, ".tmp")
                : Files.createTempFile(target.getParent(), prefix, ".tmp", PosixFilePermissions.asFileAttribute(
                        original == null ? PosixFilePermissions.fromString("rw-rw-rw-") : original.permissions()));
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            if (original != null) {
                var temporaryView = Files.getFileAttributeView(temporary, PosixFileAttributeView.class);
                if (temporaryView == null) throw new IOException("POSIX属性を引き継げません: " + target);
                preserveAccess(original, temporaryView);
            }
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    static void preserveAccess(PosixFileAttributes original, PosixFileAttributeView temporary) throws IOException {
        var current = temporary.readAttributes();
        if (!original.owner().equals(current.owner())) temporary.setOwner(original.owner());
        if (!original.group().equals(current.group())) temporary.setGroup(original.group());
        temporary.setPermissions(original.permissions());
    }
}
