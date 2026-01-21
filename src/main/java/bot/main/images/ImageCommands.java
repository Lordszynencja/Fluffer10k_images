package bot.main.images;

import static java.util.Arrays.asList;

import java.util.List;

import bot.main.util.userData.UserDataImagesUtils;
import bot.util.apis.APIUtils;
import bot.util.apis.CommandHandlers.SlashCommandHandler;
import bot.util.apis.commands.FlufferCommand;
import bot.util.apis.commands.FlufferCommandStringOption;

public class ImageCommands {
	private class ImageCommandAdder {
		private String name = null;
		private String[] aliases = new String[0];
		private String description = null;
		private boolean nsfw = false;
		private boolean hasTarget = false;

		private String answer = null;
		private String targetAnswer = null;
		private String[] aliasesTargetAnswers = null;

		public ImageCommandAdder name(final String name) {
			this.name = name;
			return this;
		}

		public ImageCommandAdder aliases(final String... aliases) {
			this.aliases = aliases;
			return this;
		}

		public ImageCommandAdder description(final String description) {
			this.description = description;
			return this;
		}

		public ImageCommandAdder nsfw() {
			nsfw = true;
			return this;
		}

		public ImageCommandAdder hasTarget() {
			hasTarget = true;
			return this;
		}

		public ImageCommandAdder answer(final String answer) {
			this.answer = answer;
			return this;
		}

		public ImageCommandAdder targetAnswer(final String targetAnswer) {
			hasTarget = true;
			this.targetAnswer = targetAnswer;
			return this;
		}

		public ImageCommandAdder aliasesTargetAnswers(final String... aliasesTargetAnswers) {
			this.aliasesTargetAnswers = aliasesTargetAnswers;
			return this;
		}

		private void add(final APIUtils apiUtils) {
			FlufferCommand cmd = new FlufferCommand(name, description)//
					.nsfw(nsfw);
			if (hasTarget) {
				cmd = cmd.addOption(new FlufferCommandStringOption("target", "target"));
			}

			final SlashCommandHandler handler = new SimpleImageCommandHandler(apiUtils, apiUtils.messageUtils,
					userDataImagesUtils, imageDir, name, answer, targetAnswer, nsfw, sfwBot);

			apiUtils.commandHandlers.addCommandHandler(cmd, handler);
			if (aliasesTargetAnswers == null) {
				for (final String alias : aliases) {
					apiUtils.commandHandlers.addCommandAlias(name, cmd.clone().name(alias));
				}
			} else {
				for (int i = 0; i < aliases.length; i++) {
					final SlashCommandHandler aliasHandler = new SimpleImageCommandHandler(apiUtils,
							apiUtils.messageUtils, userDataImagesUtils, imageDir, name, answer, aliasesTargetAnswers[i],
							nsfw, sfwBot);
					apiUtils.commandHandlers.addCommandHandler(cmd.clone().name(aliases[i]), aliasHandler);
				}
			}
		}
	}

	private ImageCommandAdder cmd(final String name, final String description) {
		return new ImageCommandAdder().name(name).description(description);
	}

	private final UserDataImagesUtils userDataImagesUtils;
	private final String imageDir;
	private final boolean sfwBot;

	public ImageCommands(final UserDataImagesUtils userDataImagesUtils, final String imageFolderPath,
			final boolean sfwBot) {
		this.userDataImagesUtils = userDataImagesUtils;

		if (imageFolderPath == null) {
			throw new RuntimeException("imageFolderPath is missing!");
		}
		imageDir = imageFolderPath;
		this.sfwBot = sfwBot;
	}

	private final List<ImageCommandAdder> commands = asList(
			cmd("angry_stare", "Stare at someone angrily").targetAnswer("%1$s stares angrily at %2$s"), //
			cmd("assgrab", "Grab some ass").nsfw().targetAnswer("%1$s grabs ass of %2$s"), //
			cmd("birthday", "Wish someone happy birthday").targetAnswer("%1$s wishes %2$s happy birthday"), //
			cmd("bite", "Bite someone").targetAnswer("%1$s bites %2$s"), //
			cmd("blowjob", "Give someone pleasure").nsfw().targetAnswer("%1$s gives a blowjob to %2$s")//
					.aliases("suck").aliasesTargetAnswers("%1$s sucks off %2$s"), //
			cmd("bonk", "Bonk someone on the head").targetAnswer("%1$s bonks %2$s"), //
			cmd("boobgrab", "Grab something soft").nsfw().targetAnswer("%1$s grabs boobs of %2$s")//
					.aliases("breastgrab", "grope", "titgrab")
					.aliasesTargetAnswers("%1$s grabs breasts of %2$s", "%1$s gropes %2$s", "%1$s grabs tits of %2$s"), //
			cmd("boobhug", "Give someone happiness").nsfw().targetAnswer("%1$s boobhugs %2$s"), //
			cmd("boop", "Boop someone").targetAnswer("%1$s boops %2$s"), //
			cmd("bootyshake", "Shake that ass").nsfw().answer("%1$s shakes the booty")
					.targetAnswer("%1$s shakes the booty for %2$s"), //
			cmd("brazil", "Send someone to Brazil").answer("%1$s goes to Brazil")
					.targetAnswer("%1$s sends %2$s to Brazil"), //
			cmd("coil", "Coil around someone").nsfw().targetAnswer("%1$s coils around %2$s"), //
			cmd("cringe", "Criiinge").answer("%1$s cringes").targetAnswer("%1$s cringes at %2$s"), //
			cmd("cuddle", "Cuddle with someone").targetAnswer("%1$s cuddles with %2$s"), //
			cmd("cum", "Orgasm!").nsfw().answer("%1$s orgasms").targetAnswer("%1$s orgasms with %2$s"), //
			cmd("cum_on", "Cum on someone else!").nsfw().answer("%1$s cums").targetAnswer("%1$s cums on %2$s"), //
			cmd("dance", "Dance dance").answer("%1$s dances").targetAnswer("%1$s dances for %2$s"), //
			cmd("drool", "heheeeee~").answer("%1$s drools").targetAnswer("%1$s drools over %2$s"), //
			cmd("everyone", "Everyone!").hasTarget(), //
			cmd("facesit", "Sit on someone's face").nsfw().targetAnswer("%1$s sat on the face of %2$s"), //
			cmd("feelbonacci", "The feels don't stop").answer(
					"https://cdn.discordapp.com/attachments/831093717376172032/831280567776706600/feelbonacci.jpg"), //
			cmd("feelsgood", "Mhmmmm~").answer(
					"https://cdn.discordapp.com/attachments/831093717376172032/831280729404211250/feelsgood.png"), //
			cmd("fuck", "You can guess what this does~").nsfw().targetAnswer("%1$s fucks with %2$s"), //
			cmd("fuck_gif", "You can guess what this does~").nsfw().targetAnswer("%1$s fucks with %2$s"), //
			cmd("gaskelly", "GAS GAS GAS").answer(
					"https://media.discordapp.net/attachments/456149873507565568/585316940193595392/image0_2.gif?comment=DO_YOU_LIKE_MY_CAR?_GUESS_YOU%27RE_READY_CAUSE_IM_WAITING_FOR_YOU._IT%27S_GONNA_BE_EXCITING!_GOT_THIS_FEELING_REALLY_DEEP_IN_MY_SOUL._LETS_GET_OUT_I_WANNA_GO_COME_ALONG_GET_IT_ON._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._DONT_BE_LAZY_CAUSE_IM_BURNING_FOR_YOU._ITS_LIKE_A_HOT_SENSATION_GOT_THIS_POWER_THAT_IS_TAKING_ME_OUT._YES_IVE_GOT_A_CRASH_ON_YOU_READY_NOW_READY_GO._GONNA_TAKE_MY_CAR_GONNA_DRIVE_IT._GONNA_DRIVE_ALONE_TILL_I_GET_YOU_CAUSE_IM_CRAZY_HOT_AND_READY_BUT_YOULL_LIKE_IT._I_WANNA_RACE_FOR_YOU_SHALL_I_GO_NOW_GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_AND_YOULL_SEE_THE_BIG_SHOW._GAS_GAS_GAS_IM_GONNA_STEP_ON_THE_GAS_TONIGHT_ILL_FLY_AND_BE_YOUR_LOVER._YEAH_YEAH_YEAH_ILL_BE_SO_QUICK_AS_A_FLASH_AND_ILL_BE_YOUR_HERO._GAS_GAS_GAS_IM_GONNA_RUN_AS_A_FLASH_TONIGHT_ILL_FIGHT_TO_BE_THE_WINNER_YEAH_YEAH_YEAH_IM_GONNA_STEP_ON_THE_GAS_ANY_YOULL_SEE_THE_BIG_SHOW"), //
			cmd("gay", "Ha! GAYYYYYYYYYYYY").hasTarget(), //
			cmd("gibhug", "Demand a hug").answer("%1$s wants a hug").targetAnswer("%1$s wants to be hugged by %2$s"), //
			cmd("gibpat", "Demand a headpat").answer("%1$s demands a pat")
					.targetAnswer("%1$s demands to be pat by %2$s"), //
			cmd("glomp", "Give someone a surprise hug!").targetAnswer("%1$s glomps %2$s"), //
			cmd("good_night", "Sleep well"), //
			cmd("handjob", "Pleasure someone").nsfw().targetAnswer("%1$s gives a handjob to %2$s"), //
			cmd("hehe", "Smile").targetAnswer("%1$s smiles at %2$s"), //
			cmd("hora", "Hora hora~").hasTarget(), //
			cmd("hug", "Hug someone").targetAnswer("%1$s hugs %2$s"), //
			cmd("humg", "Humg someone").targetAnswer("%1$s humgs %2$s"), //
			cmd("kiss", "Kiss someone").targetAnswer("%1$s kisses %2$s"), //
			cmd("lap", "Lap pillow!").answer("%1$s lays on a lap pillow")
					.targetAnswer("%1$s lets %2$s lay on their lap pillow"), //
			cmd("lick", "Lick someone").targetAnswer("%1$s licks %2$s"), //
			cmd("massage", "Massage someone").targetAnswer("%1$s massages %2$s"), //
			cmd("n", "NNNNNNNNNNNNNNNNNNNN-")
					.answer("https://cdn.discordapp.com/attachments/831093717376172032/831278978693857280/n.gif"), //
			cmd("no", "Say no").targetAnswer("%1$s says no to %2$s"), //
			cmd("nom", "Nom someone").answer("%1$s noms").targetAnswer("%1$s noms %2$s"), //
			cmd("nuzzle", "Nuzzle someone").targetAnswer("%1$s nuzzles %2$s"), //
			cmd("out", "Show someone exit")
					.answer("https://cdn.discordapp.com/attachments/831093717376172032/831280225617707058/out.jpg"), //
			cmd("padoru", "Hashire sori yo, Kaze no you ni, Tsukimihara wo, PADORU PADORU"), //
			cmd("pat", "Pat someone").targetAnswer("%1$s pats %2$s"), //
			cmd("peck", "Peck someone").targetAnswer("%1$s pecked %2$s on the cheek"), //
			cmd("peg", "Peg someone").nsfw().targetAnswer("%1$s pegs %2$s"), //
			cmd("pizza", "Give someone pizza").targetAnswer("%1$s gave pizza to %2$s"), //
			cmd("poke", "Poke someone").targetAnswer("%1$s pokes %2$s"), //
			cmd("pout", "Pout").answer("%1$s pouts").targetAnswer("%1$s pouts at %2$s"), //
			cmd("respect", "Pay respects")
					.answer("https://cdn.discordapp.com/attachments/831093717376172032/831280368782409798/f.gif"), //
			cmd("ride", "You can guess what this does~").nsfw().targetAnswer("%1$s rides %2$s"), //
			cmd("ride_gif", "You can guess what this does~").nsfw().targetAnswer("%1$s rides %2$s"), //
			cmd("saved", "Saved")
					.answer("https://cdn.discordapp.com/attachments/831093717376172032/831279675858223124/saved.jpg"), //
			cmd("shrug", "Shrug").answer("%1$s shrugs"), //
			cmd("sip", "Drink some tea").answer("%1$s sips").targetAnswer("%1$s sips with %2$s")//
					.aliases("drink").aliasesTargetAnswers("%1$s drinks tea with %2$s"), //
			cmd("sit", "Sit down").targetAnswer("%1$s sat on %2$s"), //
			cmd("slap", "Slap someone").targetAnswer("%1$s slaps %2$s"), //
			cmd("sleepcuddle", "Cuddle in bed with someone").targetAnswer("%1$s cuddles in bed with %2$s"), //
			cmd("smoosh", "Smoosh someone's face").targetAnswer("%1$s smooshes %2$s"), //
			cmd("smug", "Ara ara~").answer("%1$s smugs").targetAnswer("%1$s smugs at %2$s"), //
			cmd("snuggle", "Snuggle with someone").targetAnswer("%1$s snuggles with %2$s"), //
			cmd("space", "S P A A A C E").answer("%1$s floats in space").targetAnswer("%1$s floats in space with %2$s"), //
			cmd("spank", "Spank someone").nsfw().targetAnswer("%1$s spanks %2$s"), //
			cmd("spoderman", "Spider-man would save the world but he's busy making memes"), //
			cmd("spray", "Pshhhh").targetAnswer("%1$s sprays %2$s"), //
			cmd("stare", "Stare at someone").answer("*じーーー*").targetAnswer("*じーーー* %2$s"), //
			cmd("step", "Step on someone").nsfw().targetAnswer("%1$s steps on %2$s"), //
			cmd("sus", "Something is sus").answer("%1$s thinks something is suspicious")
					.targetAnswer("%1$s thinks %2$s is suspicious"), //
			cmd("tickle", "Tickle someone").targetAnswer("%1$s tickles %2$s"), //
			cmd("titfuck", "Milk someone with your milkers").nsfw().targetAnswer("%1$s titfucks %2$s")
					.aliases("boobjob").aliasesTargetAnswers("%1$s gives a boobjob to %2$s"), //
			cmd("vsauce", "Michael here. Or am I?")
					.answer("https://cdn.discordapp.com/attachments/831093717376172032/831279875871735888/vsauce.png"), //
			cmd("whip", "Whip someone").nsfw().targetAnswer("%1$s whips %2$s"), //
			cmd("whoping", "WHO PINGED ME?")
					.answer("https://cdn.discordapp.com/attachments/397923444072644610/439953147738193920/image.gif"), //
			cmd("work", "Work work").answer(
					"https://cdn.discordapp.com/attachments/831093717376172032/831451303489699850/work_work.jpg"), //
			cmd("yawn", "Yaaaawn").answer("%1$s yawns").targetAnswer("%1$s yawns"), //
			cmd("yes", "Say yes").targetAnswer("%1$s says yes to %2$s"));

	public void init(final APIUtils apiUtils, final boolean sfwOnly) {
		for (final ImageCommandAdder cmd : commands) {
			if (cmd.nsfw && sfwOnly) {
				continue;
			}

			cmd.add(apiUtils);
		}
	}
}
