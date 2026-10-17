package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.hijack.HijackAreaStorage;
import net.azisaba.lgw.core.hijack.HijackConfig;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AtomicConfigWriterTest {
    @TempDir Path directory;

    private FileSystem posix() throws Exception {
        // JDK ZipFS exposes real POSIX file attributes on Windows too; no external test dependency.
        return FileSystems.newFileSystem(URI.create("jar:" + directory.resolve("posix.zip").toUri()),
                Map.of("create", "true", "enablePosixFileAttributes", "true"));
    }

    @Test void nativeUtf8SaveAndFailureLeaveNoTemporaryFiles() throws Exception {
        Path file = directory.resolve("hijack.yml");
        AtomicConfigWriter.write(file, "hijack-", "日本語: 初期\n");
        AtomicConfigWriter.write(file, "hijack-", "日本語: 更新\n");
        assertEquals("日本語: 更新\n", Files.readString(file));
        Path impossible = directory.resolve("existing-directory"); Files.createDirectory(impossible);
        assertThrows(IOException.class, () -> AtomicConfigWriter.write(impossible, "hijack-", "no"));
        assertTrue(Files.isDirectory(impossible));
        try (var files = Files.list(directory)) { assertEquals(2, files.count()); }
    }

    @Test void existingGroupAccessAndDeliberatelyPrivatePermissionsSurviveReplacement() throws Exception {
        try (var fs = posix()) {
            Path file = fs.getPath("/hijack.yml"); Files.writeString(file, "before");
            var before = Files.readAttributes(file, PosixFileAttributes.class);
            for (String permission : new String[]{"rw-rw----", "rw-r-----", "rw-------", "rwxrwx---"}) {
                var expected = PosixFilePermissions.fromString(permission); Files.setPosixFilePermissions(file, expected);
                AtomicConfigWriter.write(file, "hijack-", permission);
                var after = Files.readAttributes(file, PosixFileAttributes.class);
                assertEquals(expected, after.permissions());
                assertEquals(before.owner(), after.owner()); assertEquals(before.group(), after.group());
                assertEquals(permission, Files.readString(file));
            }
        }
    }

    @Test void initialConfigUsesNormalCreationModeAndPreservesManualFixAcrossSetAndMigration() throws Exception {
        try (var fs = posix()) {
            Path file = fs.getPath("/hijack.yml"); var config = new HijackConfig(file); config.reload();
            // ZipFS does not impose an OS umask; a Unix host filters these rw bits using its umask.
            assertEquals(PosixFilePermissions.fromString("rw-rw-rw-"), Files.getPosixFilePermissions(file));
            var groupAccess = PosixFilePermissions.fromString("rw-rw----"); Files.setPosixFilePermissions(file, groupAccess);
            config.set("victory.target-points", "321");
            assertEquals(groupAccess, Files.getPosixFilePermissions(file));
            Files.writeString(file, "capture: {jump-height: 2}\nresistance: {percent: 25}\n");
            config.reload();
            assertEquals(groupAccess, Files.getPosixFilePermissions(file));
            assertEquals(25, config.getPending().number("resistance.percent"));
            assertFalse(Files.readString(file).contains("jump-height"));
        }
    }

    @Test void areaEditingPreservesMapsPermissionsAndSpawns() throws Exception {
        try (var fs = posix()) {
            Path file = fs.getPath("/maps.yml");
            Files.writeString(file, "Shagger:\n  world: maps\n  spawns: {red: {x: 1.5}}\n");
            var expected = PosixFilePermissions.fromString("rw-rw----"); Files.setPosixFilePermissions(file, expected);
            var yaml = HijackAreaStorage.save(file, "Shagger", "maps", "1", new Area3D(new Vector(0, 48, 0), new Vector(10, 52, 10)));
            assertEquals(expected, Files.getPosixFilePermissions(file));
            assertEquals(1.5, yaml.getDouble("Shagger.spawns.red.x"));
            HijackAreaStorage.save(file, "Shagger", "maps", "1", null);
            assertEquals(expected, Files.getPosixFilePermissions(file));
        }
    }

    @Test void ownerAndGroupAreRestoredBeforeFinalPermissionBits() throws Exception {
        var original = mock(PosixFileAttributes.class); var current = mock(PosixFileAttributes.class);
        var owner = mock(UserPrincipal.class); var group = mock(GroupPrincipal.class);
        when(original.owner()).thenReturn(owner); when(original.group()).thenReturn(group);
        when(current.owner()).thenReturn(mock(UserPrincipal.class)); when(current.group()).thenReturn(mock(GroupPrincipal.class));
        var permissions = PosixFilePermissions.fromString("rw-rw----"); when(original.permissions()).thenReturn(permissions);
        var view = mock(PosixFileAttributeView.class); when(view.readAttributes()).thenReturn(current);
        AtomicConfigWriter.preserveAccess(original, view);
        var order = inOrder(view); order.verify(view).setOwner(owner); order.verify(view).setGroup(group); order.verify(view).setPermissions(permissions);
    }

    @Test void inabilityToPreserveOwnershipFailsInsteadOfSilentlyReplacingAccessSettings() throws Exception {
        var original = mock(PosixFileAttributes.class); var current = mock(PosixFileAttributes.class);
        var owner = mock(UserPrincipal.class); when(original.owner()).thenReturn(owner); when(current.owner()).thenReturn(mock(UserPrincipal.class));
        var view = mock(PosixFileAttributeView.class); when(view.readAttributes()).thenReturn(current);
        doThrow(new IOException("ownership denied")).when(view).setOwner(owner);
        assertThrows(IOException.class, () -> AtomicConfigWriter.preserveAccess(original, view));
        verify(view, never()).setPermissions(anySet());
    }
}
