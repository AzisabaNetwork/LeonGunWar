package net.azisaba.lgw.core;

import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class MatchTeamVisibilityTest {
    @Test
    void newlyCreatedTeamsHideNamesFromOpponents() throws Exception {
        MatchManager manager = new MatchManager();
        Scoreboard board = mock(Scoreboard.class);
        manager.setScoreboard(board);
        for (BattleTeam team : BattleTeam.values()) {
            Team created = mock(Team.class);
            when(board.registerNewTeam(team.getTeamName())).thenReturn(created);
        }

        initializeTeams(manager);

        for (BattleTeam team : BattleTeam.values()) {
            Team created = manager.getTeams().get(team);
            // Paper 1.21.11 CraftTeam maps this value to HIDE_FOR_OTHER_TEAMS.
            verify(created).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            verify(created, never()).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
            verify(created).setAllowFriendlyFire(false);
            verify(created).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }

    @Test
    void reusedTeamsHaveTheirVisibilityRepaired() throws Exception {
        MatchManager manager = new MatchManager();
        Scoreboard board = mock(Scoreboard.class);
        manager.setScoreboard(board);
        for (BattleTeam team : BattleTeam.values()) {
            when(board.getTeam(team.getTeamName())).thenReturn(mock(Team.class));
        }

        initializeTeams(manager);

        for (Team reused : manager.getTeams().values()) {
            verify(reused).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        }
        verify(board, never()).registerNewTeam(anyString());
    }

    private static void initializeTeams(MatchManager manager) throws Exception {
        var method = MatchManager.class.getDeclaredMethod("initializeTeams");
        method.setAccessible(true);
        method.invoke(manager);
    }
}
