package net.azisaba.lgw.core.api.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class LuckPermsAPI {
    private static final LuckPermsAPI API = new LuckPermsAPI();

    public static LuckPermsAPI getApi() {
        return API;
    }

    @NonNull
    public LuckPerms luckperms() {
        return LuckPermsProvider.get();
    }

    @Nullable
    public User getUser(@NonNull UUID playerUuid) {
        return luckperms().getUserManager().getUser(playerUuid);
    }

    @Nullable
    public String getUserPrefix(@NonNull UUID playerUuid) {
        User user = getUser(playerUuid);
        if (user == null) return null;
        return getUserPrefix(user);
    }

    @Nullable
    public String getUserPrefix(@NonNull User user) {
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Nullable
    public Group getGroupByName(@NonNull String groupName) {
        return luckperms().getGroupManager().getGroup(groupName);
    }

    @Nullable
    public String getGroupName(@NonNull UUID playerUuid) {
        User user = getUser(playerUuid);
        if (user == null) return null;

        Group group = getGroupByName(user.getPrimaryGroup());
        if (group == null) return null;

        return getGroupName(group);
    }

    @NonNull
    public String getGroupName(@NonNull Group group) {
        return group.getName();
    }
}
