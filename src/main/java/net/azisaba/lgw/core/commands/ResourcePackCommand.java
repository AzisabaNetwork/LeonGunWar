package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResourcePackCommand implements CommandExecutor {

  // server.propertiesの値を取得するメソッドが見つからなかったため
  private static final String URL = "https://packs.azisaba.net/lgw.zip";

  private final String PREFIX = Chat.f("&7[&ePack&7] &r");

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です"));
      return true;
    }

    Player p = (Player) sender;

    p.setResourcePack(URL);

    p.sendMessage(Chat.f("{0}&aテクスチャの読み込みを要求しました", PREFIX));
    p.sendMessage(Chat.f("{0}&b{1}&7確認画面が出ない場合&b{1}", PREFIX, "↓"));
    p.sendMessage(
        Chat.f(
            "{0}&eサーバー選択画面&7にてアジ鯖を選択し、&e設定 &7の &cサーバーリソースパック&7を&c毎回確認&7(&cPrompt&7)にしてください",
            PREFIX));
    return true;
  }
}
