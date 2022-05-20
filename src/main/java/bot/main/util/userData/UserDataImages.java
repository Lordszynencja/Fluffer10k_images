package bot.main.util.userData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UserDataImages {
	public Map<String, Map<Long, Integer>> interactions = new HashMap<>();

	public UserDataImages() {
	}

	@SuppressWarnings("unchecked")
	public UserDataImages(final Map<String, Object> data) {
		final Map<String, Map<String, Integer>> interactionsData = (Map<String, Map<String, Integer>>) data
				.getOrDefault("interactions", new HashMap<>());
		for (final Entry<String, Map<String, Integer>> cmdEntry : interactionsData.entrySet()) {
			final Map<Long, Integer> interactionsCmdMap = new HashMap<>();
			for (final Entry<String, Integer> cmdUserEntry : cmdEntry.getValue().entrySet()) {
				interactionsCmdMap.put(Long.valueOf(cmdUserEntry.getKey()), cmdUserEntry.getValue());
			}
			interactions.put(cmdEntry.getKey(), interactionsCmdMap);
		}
	}

	public Map<String, Object> toMap() {
		final Map<String, Object> map = new HashMap<>();

		final Map<String, Object> interactionsMap = new HashMap<>();
		for (final Entry<String, Map<Long, Integer>> cmdInteractions : interactions.entrySet()) {
			final Map<String, Object> cmdUserMap = new HashMap<>();
			for (final Entry<Long, Integer> cmdUserInteractions : cmdInteractions.getValue().entrySet()) {
				cmdUserMap.put(cmdUserInteractions.getKey().toString(), cmdUserInteractions.getValue());
			}
			interactionsMap.put(cmdInteractions.getKey(), cmdUserMap);
		}
		map.put("interactions", interactionsMap);

		return map;
	}

	public int addInteraction(final String cmd, final long userId) {
		Map<Long, Integer> commandInteractions = interactions.get(cmd);
		if (commandInteractions == null) {
			commandInteractions = new HashMap<>();
			interactions.put(cmd, commandInteractions);
		}

		final int amount = commandInteractions.getOrDefault(userId, 0) + 1;
		commandInteractions.put(userId, amount);

		return amount;
	}
}
