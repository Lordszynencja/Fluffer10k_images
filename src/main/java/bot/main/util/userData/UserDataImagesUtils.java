package bot.main.util.userData;

import static bot.util.TimerUtils.startRepeatedTimedEvent;
import static bot.util.apis.CommandHandlers.addOnExit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import bot.main.Fluffer10kImages;
import bot.util.FileUtils;

public class UserDataImagesUtils {
	private static final String usersDataFilePath = "fluffer10kImages/usersData.txt";

	private Map<Long, UserDataImages> usersData = new HashMap<>();

	private void onExit() {
		saveData();
	}

	private final Fluffer10kImages fluffer10kImages;

	public UserDataImagesUtils(final Fluffer10kImages fluffer10kImages) throws IOException {
		this.fluffer10kImages = fluffer10kImages;

		loadUsersData();

		startRepeatedTimedEvent(this::saveData, 60 * 60, 0, "saving user data");

		addOnExit(this::onExit);
	}

	@SuppressWarnings("unchecked")
	private void loadUsersData() throws IOException {
		if (!new File(usersDataFilePath).exists()) {
			new File(usersDataFilePath).getParentFile().mkdirs();
			return;
		}

		final Map<String, Object> data = FileUtils.readJSONFile(usersDataFilePath);

		usersData = new HashMap<>();
		for (final Entry<String, Object> entry : data.entrySet()) {
			usersData.put(Long.valueOf(entry.getKey()), new UserDataImages((Map<String, Object>) entry.getValue()));
		}
	}

	private void saveData() {
		try {
			final Map<String, Object> data = new HashMap<>();
			for (final Entry<Long, UserDataImages> userData : usersData.entrySet()) {
				data.put(userData.getKey().toString(), userData.getValue().toMap());
			}

			try {
				FileUtils.saveJSONFileWithBackup(usersDataFilePath, data);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} catch (final Exception e) {
			fluffer10kImages.apiUtils.messageUtils.sendExceptionToMe(e);
		}
	}

	public UserDataImages getUserData(final long userId) {
		if (!usersData.containsKey(userId)) {
			usersData.put(userId, new UserDataImages());
		}

		return usersData.get(userId);
	}
}
