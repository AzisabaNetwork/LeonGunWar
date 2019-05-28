package net.azisaba.lgw.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

/**
 * コマンドの引数チェックを超簡単に！
 * タブ補完にも対応！
 *
 * @author YukiLeafX
 */
@UtilityClass
public class Args {

	public boolean isEmpty(String[] args) {
		return !get(args, 0).isPresent();
	}

	public Optional<String> get(String[] args, int index) {
		return Optional.ofNullable(args.length > index ? args[index] : null);
	}

	public boolean check(String[] args, int index, String... checks) {
		Optional<String> input = get(args, index);
		return input.isPresent() && (checks.length <= 0 || Arrays.stream(checks)
				.anyMatch(input.get()::equalsIgnoreCase));
	}

	public List<String> complete(String[] args, int index, String... suggestions) {
		Optional<String> input = get(args, index);
		return input.isPresent() && suggestions.length > 0 ? Arrays.stream(suggestions)
				.filter(suggestion -> suggestion.startsWith(input.get()))
				.collect(Collectors.toList()) : null;
	}
}
