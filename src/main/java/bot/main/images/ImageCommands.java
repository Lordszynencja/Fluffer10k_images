package bot.main.images;

import java.io.IOException;
import java.util.List;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import bot.main.util.userData.UserDataImages;
import bot.main.util.userData.UserDataImagesUtils;
import bot.util.FileUtils;
import bot.util.RandomUtils;
import bot.util.apis.APIUtils;
import bot.util.apis.CommandHandlers.SlashCommandHandler;
import bot.util.apis.MessageUtils;

public class ImageCommands {
	private static String[] loadImageUrlsFromFileForCmd(final String dir, final String cmd, final boolean nsfw) {
		try {
			final String path = dir + "imgCommands/" + cmd.replace(" ", "_") + "/"
					+ (nsfw ? "linksNSFW.txt" : "links.txt");

			return FileUtils.readFileLines(path);
		} catch (final IOException e) {
			return new String[0];
		}
	}

	private class SimpleImageCommandHandler implements SlashCommandHandler {
		private final MessageUtils messageUtils;

		private final String cmd;
		private final String answerWithoutParam;
		private final String answerWithParam;
		private final boolean isNSFW;

		private final String[] sfwLinks;
		private final String[] nsfwLinks;

		public SimpleImageCommandHandler(final MessageUtils messageUtils, final String cmd,
				final String answerWithoutParam, final String answerWithParam, final boolean isNSFW,
				final String[] sfwLinks, final String[] nsfwLinks) {
			this.messageUtils = messageUtils;
			this.cmd = cmd;
			this.answerWithoutParam = answerWithoutParam;
			this.answerWithParam = answerWithParam;
			this.isNSFW = isNSFW;
			this.sfwLinks = sfwLinks;
			this.nsfwLinks = nsfwLinks == null || nsfwLinks.length == 0 ? sfwLinks : nsfwLinks;
		}

		public SimpleImageCommandHandler(final MessageUtils messageUtils, final String cmd,
				final String answerWithoutParam, final String answerWithParam, final boolean isNSFW) {
			this(messageUtils, cmd, answerWithoutParam, answerWithParam, isNSFW,
					isNSFW ? null : loadImageUrlsFromFileForCmd(imageDir, cmd, false),
					loadImageUrlsFromFileForCmd(imageDir, cmd, true));
		}

		public SimpleImageCommandHandler(final MessageUtils messageUtils, final String cmd,
				final String answerWithoutParam, final String answerWithParam, final boolean isNSFW,
				final String urlSFW, final String urlNSFW) {
			this(messageUtils, cmd, answerWithoutParam, answerWithParam, isNSFW,
					isNSFW ? null : new String[] { urlNSFW }, urlNSFW == null ? null : new String[] { urlNSFW });
		}

		public SimpleImageCommandHandler(final MessageUtils messageUtils, final String cmd,
				final String answerWithoutParam, final String answerWithParam, final String url) {
			this(messageUtils, cmd, answerWithoutParam, answerWithParam, false, url, url);
		}

		private int addInteractionForMentions(final Long authorId, final List<Long> mentionIds) {
			int min = Integer.MAX_VALUE;
			for (final long userId : mentionIds) {
				final UserDataImages userData = userDataImagesUtils.getUserData(userId);
				final int amount = userData.addInteraction(cmd, authorId);
				if (amount < min) {
					min = amount;
				}
			}

			return min;
		}

		@Override
		public void handle(final SlashCommandInteraction interaction) {
			final boolean isNSFWChannel = MessageUtils.isNSFWChannel(interaction);
			if (!isNSFWChannel && isNSFW) {
				MessageUtils.sendEphemeralMessage(interaction, "This command cannot be used here");
				return;
			}

			final InteractionImmediateResponseBuilder responder = interaction.createImmediateResponder();

			final EmbedBuilder embed = new EmbedBuilder();
			final String userName = interaction.getUser().getDisplayName(interaction.getServer().get());
			final String argument = interaction.getArgumentStringValueByIndex(0).orElse(null);
			if (argument != null) {
				if (answerWithParam != null) {
					embed.setDescription(String.format(answerWithParam, userName,
							messageUtils.replaceMentionsWithUserNames(argument, interaction.getServer().get())));
				}

				final List<Long> mentionIds = MessageUtils.getUserMentionIds(argument);
				if (!mentionIds.isEmpty()) {
					final int amount = addInteractionForMentions(interaction.getUser().getId(), mentionIds);
					final String footerText = "You did it " + (amount == 1 ? "1 time" : amount + " times");
					embed.setFooter(footerText);
				}

				responder.append(String.join(" ", MessageUtils.getMentions(argument)));
			} else {
				if (answerWithoutParam != null) {
					embed.setDescription(String.format(answerWithoutParam, userName));
				}
			}

			final String[] picsList = isNSFWChannel ? nsfwLinks : sfwLinks;
			final String imgUrl = RandomUtils.getRandom(picsList);
			embed.setImage(imgUrl);

			responder.addEmbed(embed).respond();
		}
	}

	private final UserDataImagesUtils userDataImagesUtils;
	private final String imageDir;

	public ImageCommands(final UserDataImagesUtils userDataImagesUtils, final String imageFolderPath) {
		this.userDataImagesUtils = userDataImagesUtils;

		if (imageFolderPath == null) {
			throw new RuntimeException("imageFolderPath is missing!");
		}
		imageDir = imageFolderPath;
	}

	private void addImageCommandSFW(final APIUtils apiUtils, final String cmd, final String description,
			final String answerWithoutParam, final String answerWithParam, final String... aliases) {
		addImageCommand(apiUtils, cmd, description, answerWithoutParam, answerWithParam, false, aliases);
	}

	private void addImageCommandNSFW(final APIUtils apiUtils, final String cmd, final String description,
			final String answerWithoutParam, final String answerWithParam, final String... aliases) {
		addImageCommand(apiUtils, cmd, description, answerWithoutParam, answerWithParam, true, aliases);
	}

	private void addImageCommand(final APIUtils apiUtils, final String cmd, final String description,
			final String answerWithoutParam, final String answerWithParam, final boolean isNSFW,
			final String... aliases) {
		final SlashCommandHandler handler = new SimpleImageCommandHandler(apiUtils.messageUtils, cmd,
				answerWithoutParam, answerWithParam, isNSFW);

		final SlashCommandBuilder scb = SlashCommand.with(cmd, description)//
				.addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "target", "target"));
		apiUtils.commandHandlers.addSlashCommandHandler(cmd, handler, scb);
		for (final String alias : aliases) {
			final SlashCommandBuilder scbAlias = SlashCommand.with(alias, description)//
					.addOption(SlashCommandOption.create(SlashCommandOptionType.STRING, "target", "target"));
			apiUtils.commandHandlers.addCommandAlias(cmd, alias, scbAlias);
		}
	}

	private void addTargetlessImageCommandWithoutAnswer(final APIUtils apiUtils, final String cmd,
			final String description, final String url, final String... aliases) {
		addTargetlessImageCommand(apiUtils, cmd, description, null, null, url, aliases);
	}

	private void addTargetlessImageCommand(final APIUtils apiUtils, final String cmd, final String description,
			final String answerWithoutParam, final String answerWithParam, final String url, final String... aliases) {
		final SlashCommandHandler handler = new SimpleImageCommandHandler(apiUtils.messageUtils, cmd,
				answerWithoutParam, answerWithParam, url);

		final SlashCommandBuilder scb = SlashCommand.with(cmd, description);
		apiUtils.commandHandlers.addSlashCommandHandler(cmd, handler, scb);
		for (final String alias : aliases) {
			final SlashCommandBuilder scbAlias = SlashCommand.with(alias, description);
			apiUtils.commandHandlers.addCommandAlias(cmd, alias, scbAlias);
		}
	}

	public void init(final APIUtils apiUtils, final boolean sfwOnly) {
		addImageCommandSFW(apiUtils, "angry_stare", "Stare at someone angrily", null, "%1$s stares angrily at %2$s");
		addImageCommandSFW(apiUtils, "birthday", "Wish someone happy birthday", null,
				"%1$s wishes %2$s happy birthday");
		addImageCommandSFW(apiUtils, "bite", "Bite someone", null, "%1$s bites %2$s");
		addImageCommandSFW(apiUtils, "bonk", "Bonk someone on the head", null, "%1$s bonks %2$s");
		addImageCommandSFW(apiUtils, "boop", "Boop someone", null, "%1$s boops %2$s");
		addImageCommandSFW(apiUtils, "brazil", "Send someone to Brazil", "%1$s goes to Brazil",
				"%1$s sends %2$s to Brazil");
		addImageCommandSFW(apiUtils, "cringe", "Criiinge", "%1$s cringes", "%1$s cringes at %2$s");
		addImageCommandSFW(apiUtils, "cuddle", "Cuddle with someone", null, "%1$s cuddles with %2$s");
		addImageCommandSFW(apiUtils, "dance", "Dance dance", "%1$s dances", "%1$s dances for %2$s");
		addImageCommandSFW(apiUtils, "drool", "heheeeee~", "%1$s drools", "%1$s drools over %2$s");
		addImageCommandSFW(apiUtils, "everyone", "Everyone!", null, null);
		addImageCommandSFW(apiUtils, "gay", "Ha! GAYYYYYYYYYYYY", null, null);
		addImageCommandSFW(apiUtils, "gibhug", "Demand a hug", "%1$s wants a hug", "%1$s wants to be hugged by %2$s");
		addImageCommandSFW(apiUtils, "gibpat", "Demand a headpat", "%1$s demands a pat",
				"%1$s demands to be pat by %2$s");
		addImageCommandSFW(apiUtils, "glomp", "Give someone a surprise hug!", null, "%1$s glomps %2$s");
		addImageCommandSFW(apiUtils, "good_night", "Sleep well", null, null);
		addImageCommandSFW(apiUtils, "hehe", "Smile", null, null);
		addImageCommandSFW(apiUtils, "hora", "Hora hora~", null, null);
		addImageCommandSFW(apiUtils, "hug", "Hug someone", null, "%1$s hugs %2$s");
		addImageCommandSFW(apiUtils, "kiss", "Kiss someone", null, "%1$s kisses %2$s");
		addImageCommandSFW(apiUtils, "lap", "Lap pillow!", "%1$s lays on a lap pillow",
				"%1$s lets %2$s lay on their lap pillow");
		addImageCommandSFW(apiUtils, "lick", "Lick someone", null, "%1$s licks %2$s");
		addImageCommandSFW(apiUtils, "massage", "Massage someone", null, "%1$s massages %2$s");
		addImageCommandSFW(apiUtils, "no", "Say no", null, null);
		addImageCommandSFW(apiUtils, "nom", "Nom someone", "%1$s noms", "%1$s noms %2$s");
		addImageCommandSFW(apiUtils, "nuzzle", "Nuzzle someone", null, "%1$s nuzzles %2$s");
		addImageCommandSFW(apiUtils, "padoru", "Hashire sori yo, Kaze no you ni, Tsukimihara wo, PADORU PADORU", null,
				null);
		addImageCommandSFW(apiUtils, "pat", "Pat someone", null, "%1$s pats %2$s");
		addImageCommandSFW(apiUtils, "peck", "Peck someone", null, "%1$s pecked %2$s on the cheek");
		addImageCommandSFW(apiUtils, "pizza", "Give someone pizza", null, "%1$s gave pizza to %2$s");
		addImageCommandSFW(apiUtils, "poke", "Poke someone", null, "%1$s pokes %2$s");
		addImageCommandSFW(apiUtils, "pout", "Pout", "%1$s pouts", "%1$s pouts at %2$s");
		addImageCommandSFW(apiUtils, "shrug", "Shrug", "%1$s shrugs", "%1$s shrugs");
		addImageCommandSFW(apiUtils, "sip", "Drink some tea", "%1$s sips", "%1$s sips with %2$s", "drink");
		addImageCommandSFW(apiUtils, "sit", "Sit down", null, "%1$s sat on %2$s");
		addImageCommandSFW(apiUtils, "slap", "Slap someone", null, "%1$s slaps %2$s");
		addImageCommandSFW(apiUtils, "sleepcuddle", "Cuddle in bed with someone", null,
				"%1$s cuddles in bed with %2$s");
		addImageCommandSFW(apiUtils, "smoosh", "Smoosh someone's face", null, "%1$s smooshes %2$s");
		addImageCommandSFW(apiUtils, "smug", "Ara ara~", "%1$s smugs", "%1$s smugs at %2$s");
		addImageCommandSFW(apiUtils, "snuggle", "Snuggle with someone", null, "%1$s snuggles with %2$s");
		addImageCommandSFW(apiUtils, "space", "S P A A A C E", "%1$s floats in space",
				"%1$s floats in space with %2$s");
		addImageCommandSFW(apiUtils, "spoderman", "Spider-man would save the world but he's busy making memes", null,
				null);
		addImageCommandSFW(apiUtils, "spray", "Pshhhh", null, null);
		addImageCommandSFW(apiUtils, "stare", "Stare at someone", "*じーーー*", "*じーーー* %2$s");
		addImageCommandSFW(apiUtils, "sus", "Something is sus", "%1$s thinks something is suspicious",
				"%1$s thinks %2$s is suspicious");
		addImageCommandSFW(apiUtils, "tickle", "Tickle someone", null, "%1$s tickles %2$s");
		addImageCommandSFW(apiUtils, "yawn", "Yaaaawn", "%1$s yawns", "%1$s yawns");
		addImageCommandSFW(apiUtils, "yes", "Say yes", null, null);

		if (!sfwOnly) {
			addImageCommandNSFW(apiUtils, "assgrab", "Grab some ass", null, "%1$s grabs ass of %2$s");
			addImageCommandNSFW(apiUtils, "blowjob", "Give someone pleasure", null, "%1$s sucks off %2$s", "suck");
			addImageCommandNSFW(apiUtils, "boobgrab", "Grab something soft", null, "%1$s grabs boobs of %2$s",
					"breastgrab", "grope", "titgrab");
			addImageCommandNSFW(apiUtils, "boobhug", "Give someone happiness", null, "%1$s boobhugs %2$s");
			addImageCommandNSFW(apiUtils, "bootyshake", "Shake that ass", "%1$s shakes the booty",
					"%1$s shakes the booty for %2$s");
			addImageCommandNSFW(apiUtils, "coil", "Coil around someone", null, "%1$s coils around %2$s");
			addImageCommandNSFW(apiUtils, "facesit", "Sit on someone's face", null, "%1$s sat on the face of %2$s");
			addImageCommandNSFW(apiUtils, "fuck", "You can guess what this does~", null, "%1$s fucks with %2$s");
			addImageCommandNSFW(apiUtils, "fuck_gif", "You can guess what this does~", null, "%1$s fucks with %2$s");
			addImageCommandNSFW(apiUtils, "handjob", "Pleasure someone", null, "%1$s gives a handjob to %2$s");
			addImageCommandNSFW(apiUtils, "peg", "Peg someone", null, "%1$s pegs %2$s");
			addImageCommandNSFW(apiUtils, "spank", "Spank someone", null, "%1$s spanks %2$s");
			addImageCommandNSFW(apiUtils, "step", "Step on someone", null, "%1$s steps on %2$s");
			addImageCommandNSFW(apiUtils, "titfuck", "Milk someone with your milkers", null, "%1$s titfucks %2$s",
					"boobjob");
			addImageCommandNSFW(apiUtils, "whip", "Whip someone", null, "%1$s whips %2$s");
		}

		addTargetlessImageCommandWithoutAnswer(apiUtils, "feelbonacci", "The feels don't stop",
				"https://cdn.discordapp.com/attachments/831093717376172032/831280567776706600/feelbonacci.jpg");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "feelsgood", "Mhmmmm~",
				"https://cdn.discordapp.com/attachments/831093717376172032/831280729404211250/feelsgood.png");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "gaskelly", "GAS GAS GAS",
				"https://media.discordapp.net/attachments/456149873507565568/585316940193595392/image0_2.gif?comment=DO_YOU_LIKE_MY_CAR?_GUESS_YOU%27RE_READY_CAUSE_IM_WAITING_FOR_YOU._IT%27S_GONNA_BE_EXCITING!_GOT_THIS_FEELING_REALLY_DEEP_IN_MY_SOUL._LETS_GET_OUT_I_WANNA_GO_COME_ALONG_GET_IT_ON._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._DONT_BE_LAZY_CAUSE_IM_BURNING_FOR_YOU._ITS_LIKE_A_HOT_SENSATION_GOT_THIS_POWER_THAT_IS_TAKING_ME_OUT._YES_IVE_GOT_A_CRASH_ON_YOU_READY_NOW_READY_GO._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW_GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_ANY_YOULL_SEE_THE_BIG_SHOW");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "n", "NNNNNNNNNNNNNNNNNNNN-",
				"https://cdn.discordapp.com/attachments/831093717376172032/831278978693857280/n.gif");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "out", "Show someone exit",
				"https://cdn.discordapp.com/attachments/831093717376172032/831280225617707058/out.jpg");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "respect", "Pay respects",
				"https://cdn.discordapp.com/attachments/831093717376172032/831280368782409798/f.gif");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "saved", "Saved",
				"https://cdn.discordapp.com/attachments/831093717376172032/831279675858223124/saved.jpg");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "vsauce", "Michael here. Or am I?",
				"https://cdn.discordapp.com/attachments/831093717376172032/831279875871735888/vsauce.png");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "whoping", "WHO PINGED ME?",
				"https://cdn.discordapp.com/attachments/397923444072644610/439953147738193920/image.gif");
		addTargetlessImageCommandWithoutAnswer(apiUtils, "work", "Work work",
				"https://cdn.discordapp.com/attachments/831093717376172032/831451303489699850/work_work.jpg");
	}
}
