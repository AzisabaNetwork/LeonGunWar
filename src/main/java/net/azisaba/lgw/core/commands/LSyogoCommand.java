package net.azisaba.lgw.core.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.SyogoData;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LSyogoCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(command.getName().equalsIgnoreCase("lsyogo")){
            if(args.length == 0){
                return false;
            }else {
                if(args[0].equalsIgnoreCase("add")){
                    if(!sender.hasPermission("leongunwar.syogo.add")){
                        sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        return true;
                    }
                    if(args.length >= 3) {
                        LeonGunWar.getPlugin().getSyogoConfig().add(args[1],args[2]);
                        sender.sendMessage(Chat.f("&b称号 {0}({1}&r) を追加しました。",args[1],args[2]));
                    }
                }else if(args[0].equalsIgnoreCase("give")){
                    if(!sender.hasPermission("leongunwar.syogo.give")){
                        sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        return true;
                    }
                    if(args.length >= 3){
                        Player player = Bukkit.getPlayer(args[1]);
                        if(player != null){
                            if(SyogoData.getSyogoDataFromCache(player.getUniqueId()) == null){
                                SyogoData data = new SyogoData(player.getUniqueId(),player.getName(),args[2]);
                                sender.sendMessage(Chat.f("&e処理中です...お待ち下さい..."));
                                Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                    @Override
                                    public void run() {
                                        data.give();
                                        sender.sendMessage(Chat.f("&b{0}&e に称号 &c{1} &eを付与しました！",args[1],args[2]));
                                    }
                                });
                            }else {
                                sender.sendMessage(Chat.f("&cそのプレイヤーは既に称号を所持しています！"));
                            }
                        }else {
                            sender.sendMessage(Chat.f("&cプレイヤーはオフラインです！"));
                            sender.sendMessage(Chat.f("&e確認中です...お待ち下さい..."));
                            Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                @Override
                                public void run() {
                                    SyogoData data = SyogoData.getSyogoData(args[1]);
                                    if(data == null){
                                        Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                                        User user = essentials.getUser(args[1]);
                                        if(user == null){
                                            sender.sendMessage(Chat.f("&cプレイヤーが見つかりませんでした。"));
                                        }else {
                                            data = new SyogoData(user.getConfigUUID(), args[1], args[2]);
                                            sender.sendMessage(Chat.f("&e処理中です...お待ち下さい..."));
                                            data.give();
                                            sender.sendMessage(Chat.f("&b{0}&a に称号 &c{1} &aを付与しました！", args[1], args[2]));
                                        }
                                    }else {
                                        sender.sendMessage(Chat.f("&cそのプレイヤーは既に称号 &c{0} &aを所持しています！",data.getSyogo()));
                                    }
                                }
                            });
                        }
                    }
                }else if(args[0].equalsIgnoreCase("remove")){
                    if(!sender.hasPermission("leongunwar.syogo.remove")){
                        sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        return true;
                    }
                    if(args.length >= 2){
                        if(sender.hasPermission("leongunwar.syogo.remove-admin")){
                            Player player = Bukkit.getPlayer(args[1]);
                            if(player != null){
                                SyogoData data = SyogoData.getSyogoDataFromCache(player.getUniqueId());
                                if(data != null){
                                    sender.sendMessage(Chat.f("&e処理中です...お待ち下さい..."));
                                    Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                        @Override
                                        public void run() {
                                            data.remove();
                                            sender.sendMessage(Chat.f("&b{0}&a の称号を削除しました！",args[1]));
                                        }
                                    });
                                }else {
                                    sender.sendMessage(Chat.f("&cそのプレイヤーは称号を所持していません！"));
                                }
                            }else {
                                sender.sendMessage(Chat.f("&cプレイヤーはオフラインです！"));
                                sender.sendMessage(Chat.f("&e確認中です...お待ち下さい..."));
                                Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                    @Override
                                    public void run() {
                                        SyogoData data = SyogoData.getSyogoData(args[1]);
                                        if(data != null){
                                            sender.sendMessage(Chat.f("&e処理中です...お待ち下さい..."));
                                            data.remove();
                                            sender.sendMessage(Chat.f("&b{0}&a の称号を削除しました！",args[1]));
                                        }else {
                                            sender.sendMessage(Chat.f("&cそのプレイヤーは称号を所持していません！"));
                                        }
                                    }
                                });
                            }
                        }else {
                            sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        }
                        return true;
                    }
                    if(sender instanceof Player){
                        Player sp = (Player)sender;
                        SyogoData data = SyogoData.getSyogoDataFromCache(sp.getUniqueId());
                        if(data != null){
                            sender.sendMessage(Chat.f("&e処理中です...お待ち下さい..."));
                            Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                @Override
                                public void run() {
                                    data.remove();
                                    sender.sendMessage(Chat.f("&bあなたの称号を削除しました。"));
                                }
                            });
                        }else {
                            sender.sendMessage(Chat.f("&cあなたは称号をもっていません！"));
                        }
                    }
                }else if(args[0].equalsIgnoreCase("check")){
                    if(!sender.hasPermission("leongunwar.syogo.check")){
                        sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        return true;
                    }
                    if(args.length >= 2){
                        Player p = Bukkit.getPlayer(args[1]);
                        SyogoData data;
                        if(p != null){
                            data = SyogoData.getSyogoDataFromCache(p.getUniqueId());
                            if(data != null){
                                sender.sendMessage(Chat.f("&eプレイヤー {0} は称号 {1} を持っています",args[1],data.getSyogo()));
                            }else {
                                sender.sendMessage(Chat.f("&cプレイヤー {0} は称号を持っていません",args[1]));
                            }
                        }else {
                            sender.sendMessage(Chat.f("&cプレイヤーはオフラインです！"));
                            sender.sendMessage(Chat.f("&e確認中です...お待ち下さい..."));
                            Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), new Runnable() {
                                @Override
                                public void run() {
                                    SyogoData data = SyogoData.getSyogoData(args[1]);
                                    if(data != null){
                                        sender.sendMessage(Chat.f("&eプレイヤー {0} は称号 {1} を持っています",args[1],data.getSyogo()));
                                    }else {
                                        sender.sendMessage(Chat.f("&cプレイヤー {0} は称号を持っていません",args[1]));
                                    }
                                }
                            });
                        }
                    }
                }else if(args[0].equalsIgnoreCase("delete")){
                    if(!sender.hasPermission("leongunwar.syogo.delete")){
                        sender.sendMessage(Chat.f("&cあなたはこのコマンドを実行する権限を持っていません！"));
                        return true;
                    }
                    if(args.length >= 2) {
                        LeonGunWar.getPlugin().getSyogoConfig().delete(args[1]);
                        sender.sendMessage(Chat.f("&b称号 &c{1} &bを削除しました。",args[1]));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(command.getName().equalsIgnoreCase("lsyogo")){
            List<String> list = new ArrayList<>();
            if(args.length <= 1){
                list.add("add");
                list.add("give");
                list.add("remove");
                list.add("check");
                list.add("delete");
            }
            return list;
        }
        return null;
    }
}
