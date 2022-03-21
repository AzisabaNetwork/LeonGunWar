package net.azisaba.lgw.core.util;

import lombok.NonNull;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class SyogoData {

    private static final String INSERT_SYOGO = "INSERT INTO syogos (uuid,name,syogo) VALUES (?,?,?)";

    private static final String SELECT_SYOGO = "SELECT * FROM syogos WHERE uuid=?";
    private static final String SELECT_SYOGO_BY_NAME = "SELECT * FROM syogos WHERE name=?";
    private static final String UPDATE_NAME = "UPDATE syogos SET name=? WHERE uuid=?";

    private static final String DELETE_SYOGO = "DELETE FROM syogos WHERE uuid=?";

    private static final HashMap<UUID,SyogoData> cache = new HashMap<>();

    private final UUID uuid;
    private String name;
    @NonNull
    private final String syogo;

    public SyogoData(UUID uuid, String name, String syogo){
        this.uuid = uuid;
        this.name = name;
        this.syogo = syogo;
    }

    public static SyogoData getSyogoDataFromCache(UUID uuid){
        return cache.get(uuid);
    }

    public static SyogoData getSyogoData(UUID uuid){

        SyogoData data = cache.get(uuid);
        if(data != null){
            return data;
        }

        try(ResultSet s = LeonGunWar.getPlugin().getSqlConnection().executeQuery(SELECT_SYOGO,uuid)) {
            if(s.next()){
                data = new SyogoData(UUID.fromString(s.getString("uuid")),s.getString("name"),s.getString("syogo"));
                Player p = Bukkit.getPlayer(data.uuid);
                if(p != null && !p.getName().equals(data.name)){
                    data.name = p.getName();
                    Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            LeonGunWar.getPlugin().getSqlConnection().executeUpdate(UPDATE_NAME,p.getName(),p.getUniqueId().toString());
                        }
                    });
                }
                cache.put(uuid,data);
                return data;
            }
            cache.put(uuid,null);
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        cache.put(uuid,null);
        return null;
    }

    public static SyogoData getSyogoData(String name){
        try(ResultSet s = LeonGunWar.getPlugin().getSqlConnection().executeQuery(SELECT_SYOGO_BY_NAME,name)) {
            if(s.next()){
                return new SyogoData(UUID.fromString(s.getString("uuid")),s.getString("name"),s.getString("syogo"));
            }
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public boolean give(){
        cache.put(this.uuid,this);
        try {
            if(LeonGunWar.getPlugin().getSqlConnection().executeQuery(SELECT_SYOGO).next()){
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        LeonGunWar.getPlugin().getSqlConnection().executeUpdate(INSERT_SYOGO,this.uuid,this.name,this.syogo);
        return true;
    }

    public void remove(){
        cache.remove(this.uuid);
        LeonGunWar.getPlugin().getSqlConnection().executeUpdate(DELETE_SYOGO,this.uuid);
    }

    @NonNull
    public String getSyogo(){
        return this.syogo;
    }

    public static void removeCache(UUID uuid){
        cache.remove(uuid);
    }
    public static void clearCache(){
        cache.clear();
    }

}
