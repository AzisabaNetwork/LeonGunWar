package net.azisaba.lgw.core.utils;

import java.text.MessageFormat;

import org.bukkit.ChatColor;

public class Chat {

	// インスタンス作成を無効化
	protected Chat() {
	}

	// メッセージをフォーマットして、&で色をつける
	public static String f(String text, Object... args) {
		return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', text), args);
	}

	// 色を消す
	public static String r(String text) {
		return ChatColor.stripColor(text);
	}
}
