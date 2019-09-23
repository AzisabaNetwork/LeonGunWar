package net.azisaba.lgw.core.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecondOfDay {

    // 1日のうちの秒として取得し、日本語化
    public String f(long secondOfDay) {
        LocalTime time = LocalTime.ofSecondOfDay(secondOfDay);
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(time.getHour() > 0 ? "H時間" : "")
                .appendPattern(time.getMinute() > 0 ? "m分" : "")
                .appendPattern(time.getSecond() > 0 ? "s秒" : "")
                .toFormatter();
        return time.format(formatter);
    }

    // 1日のうちの秒として取得し、シンプルに文字列化
    public String toString(long secondOfDay) {
        return LocalTime.ofSecondOfDay(secondOfDay).format(DateTimeFormatter.ISO_TIME);
    }
}
