package net.azisaba.lgw.core.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class SecondOfDay {

	protected SecondOfDay() {
	}

	// 1日のうちの秒として取得し、日本語化
	public static String f(int secondOfDay) {
		LocalTime time = LocalTime.ofSecondOfDay(secondOfDay);
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern(time.getHour() > 0 ? "H時間" : "")
				.appendPattern(time.getMinute() > 0 ? "m分" : "")
				.appendPattern("s秒")
				.toFormatter();
		return time.format(formatter);
	}

	// 1日のうちの秒として取得し、シンプルに文字列化
	public static String toString(int secondOfDay) {
		return LocalTime.ofSecondOfDay(secondOfDay).toString();
	}
}
