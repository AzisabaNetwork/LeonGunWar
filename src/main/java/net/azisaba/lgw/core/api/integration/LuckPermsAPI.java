package net.azisaba.lgw.core.api.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Entrypoint of LuckPerms integration
 */
public class LuckPermsAPI {
    private static final LuckPermsAPI API = new LuckPermsAPI();

    public static LuckPermsAPI getApi() {
        return API;
    }

    /**
     * Get LuckPerms Instance
     * @return instance of luckperms
     */
    @NonNull
    public LuckPerms luckperms() {
        return LuckPermsProvider.get();
    }

    /**
     * Get User by UUID
     * @param playerUuid uuid of target player
     * @return user. If failure, null.
     */
    @Nullable
    public User getUser(@NonNull UUID playerUuid) {
        return luckperms().getUserManager().getUser(playerUuid);
    }

    /**
     * Get User prefix by UUID
     * @param playerUuid uuid of target player
     * @return prefix. If failure, null.
     */
    @Nullable
    public String getUserPrefix(@NonNull UUID playerUuid) {
        User user = getUser(playerUuid);
        if (user == null) return null;
        return getUserPrefix(user);
    }

    /**
     * Get User prefix by User instance
     * @param user instance of target player
     * @return prefix. If failure, null.
     */
    @Nullable
    public String getUserPrefix(@NonNull User user) {
        return user.getCachedData().getMetaData().getPrefix();
    }

    /**
     * Get Group by name
     * @param groupName name of group
     * @return group. If failure, null.
     */
    @Nullable
    public Group getGroupByName(@NonNull String groupName) {
        return luckperms().getGroupManager().getGroup(groupName);
    }

    /**
     * Get Group name by UUID
     * @param playerUuid uuid of target player
     * @return group name. If failure, null.
     */
    @Nullable
    public String getGroupName(@NonNull UUID playerUuid) {
        User user = getUser(playerUuid);
        if (user == null) return null;

        Group group = getGroupByName(user.getPrimaryGroup());
        if (group == null) return null;

        return getGroupName(group);
    }

    /**
     * Get group name by Group instance
     * @param group instance of target group
     * @return group name
     */
    @NonNull
    public String getGroupName(@NonNull Group group) {
        return group.getName();
    }
}
