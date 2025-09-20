package net.azisaba.lgw.core.api.limit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LimitActionAPITest {
    private static final UUID PLAYER_ALPHA = UUID.fromString("140cc316-45c8-4854-8cc3-1645c88854eb");
    private static final UUID PLAYER_BETA = UUID.fromString("78cf93d0-dad0-454b-8f93-d0dad0654b2e");

    private LimitActionAPI instance = new LimitActionAPI();

    @BeforeEach
    public void beforeEach() {
        instance = new LimitActionAPI();
    }

    @Test
    public void checkToggleDrop() {
        assertFalse(instance.isAllowedDrop(PLAYER_ALPHA));
        instance.toggleAllowDrop(PLAYER_ALPHA);
        assertTrue(instance.isAllowedDrop(PLAYER_ALPHA));
        instance.toggleAllowDrop(PLAYER_ALPHA);
        assertFalse(instance.isAllowedDrop(PLAYER_ALPHA));
    }

    @Test
    public void checkToggleBuild() {
        assertFalse(instance.isAllowedBuild(PLAYER_BETA));
        instance.toggleAllowBuild(PLAYER_BETA);
        assertTrue(instance.isAllowedBuild(PLAYER_BETA));
        instance.toggleAllowBuild(PLAYER_BETA);
        assertFalse(instance.isAllowedBuild(PLAYER_BETA));
    }
}
