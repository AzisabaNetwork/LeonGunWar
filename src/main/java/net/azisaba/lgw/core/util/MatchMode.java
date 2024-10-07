package net.azisaba.lgw.core.util;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import net.azisaba.lgw.core.utils.Chat;

//@Getter
//@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MatchMode {

    TEAM_DEATH_MATCH(
            Chat.f("&9チームデスマッチ"),
            Chat.f("&9TDM"),
            Chat.f("&7先に 50キル &7で勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("tdm", "teamdeathmatch", "team"),
            true),

    TEAM_DEATH_MATCH_NOLIMIT(
            Chat.f("&6上限なしチームデスマッチ"),
            Chat.f("&6TDM-NOLIMIT"),
            Chat.f("&7終了時に &cキル数が多いチーム &7が勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("tdmn", "nolimit", "no-limit", "teamdeathmatchnolimit", "tdm-nolimit", "tdm-no-limit", "team-no-limit", "team-nolimit"),
            true),

    LEADER_DEATH_MATCH(
            Chat.f("&dリーダーデスマッチ"),
            Chat.f("&dLDM"),
            Chat.f("&7相手チームの &dリーダー &7を倒して勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("ldm", "leaderdeathmatch", "leader"),
            true),

    LEADER_DEATH_MATCH_POINT(
            Chat.f("&eポイント制リーダーデスマッチ"),
            Chat.f("&eLDM-POINT"),
            Chat.f("&7終了時に &cポイントが多いチーム &7が勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("ldmp", "ldm-point", "leaderdeathmatchpoint", "leader-point"),
            true),

    CUSTOM_DEATH_MATCH(
            Chat.f("&bカスタムデスマッチ"),
            Chat.f("&bCDM"),
            Chat.f("&7募集時にかかれていた条件を達成で勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("cdm", "customdeathmatch", "custom"),
            true),

    ASSASSINATION_MATCH(
            Chat.f("&eアサシネーション"),
            Chat.f("&eASN"),
            Chat.f("&7相手チームのウィザーをキルまたは、試合終了時に &c残りHPが多いチーム &7が勝利"),
            Duration.ofMinutes(10),
            Arrays.asList("asn", "assassinationmatch", "assasination"),
            false);


    private final String modeName;
    private final String shortModeName;
    private final String description;
    private final Duration duration;
    @Getter
    private final List<String> suggests;
    private final boolean killingplayergetpoint;

    MatchMode(String modeName,String shortModeName,String description,Duration duration,List<String> suggests,boolean killingplayergetpoint) {
        this.modeName = modeName;
        this.shortModeName = shortModeName;
        this.description = description;
        this.duration = duration;
        this.suggests = suggests;
        this.killingplayergetpoint = killingplayergetpoint;
    }

    public static MatchMode getFromString(String text) {
        String suggest = text.replace(" ", "").toLowerCase();
        return Arrays.stream(values())
                .filter(mode -> mode.suggests.contains(suggest))
                .findAny()
                .orElse(null);
    }

    public Duration getDuration() { return duration; }

    public Object getDescription() { return description; }

    public String getShortModeName() { return shortModeName; }

    public String getModeName() {
        return modeName;
    }

    public boolean getKillingPlayerGetPoint(){
        return killingplayergetpoint;
    }
}
