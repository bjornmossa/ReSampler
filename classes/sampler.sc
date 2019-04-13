Sampler {
	var path = nil;
	var soundFile = nil;
	var sfView = nil;
	var player = nil;
	var loop = false;
	var rate = 1;
	var gain = 1;
	var openButton = nil;
	var buffer = nil;
	var <>view;

	*new {
		|parent, xOffset, yOffset|
		^super.new.init(parent, xOffset, yOffset);
	}

	init {
		|parent, xOffset, yOffset|

		var playButton;
		var loopTrigger;
		var rateNum;
		var rateKnob;
		var gainNum;
		var gainKnob;
		var buttonsView;
		var mainView;
		var width;
		var height;
		var sf_height;
		var btn_height;

		width = parent.bounds.width / 2;
		height = (parent.bounds.height / 2) - 10;

		btn_height = height / 5;
		sf_height = height - btn_height;

		mainView = CompositeView.new(parent, xOffset, yOffset, width, height);

		mainView.background = Color.rand;

		sfView = SoundFileView.new(mainView, Rect(0, btn_height, width, sf_height));
		sfView.elasticMode = true;
        sfView.gridOn = false;
		soundFile = SoundFile.new();

		// GUI elements initialization

		openButton = Button().states_([
			["select file"]
		]).action_({
			FileDialog(
				{
					|file|
					var filename, outpath, test;
					path = file[0];
					filename = PathName(path).fileName;
					outpath = PathName.tmp++filename;
					soundFile.openRead(path);
					soundFile.numFrames.postln;
					soundFile.normalize(outpath);
					soundFile.openRead(outpath);
					sfView.soundfile = soundFile;
					sfView.read(0, soundFile.numFrames);
					sfView.refresh;
				},
				{},
				1,
				0
			);
		});

		playButton = Button().states_([
			["play"]
		]).action_({
			var start, end;
			start = sfView.selection(0)[0];
			end = sfView.selection(0)[1];
			if (end == 0, {end = -1});
			buffer = Buffer.read(Server.local, path, start, end);

			if (player.isPlaying,
				{
					player.free;
				}
			);

			this.play(buffer);
		});

		loopTrigger = CheckBox()
		.string_("loop")
		.action_({
			|checkbox|
			if (checkbox.value,
				{loop = true},
				{
					loop = false;
					if (player.isPlaying,
						{
							player.set(\loop, 0);
						}
					);
				},
			);
		});

		rateKnob = Knob()
		.value_(rate)
		.action_({
			|knob|
			rate = knob.value.linlin(0.0, 1.0, -2, 2);
			if (player.isPlaying,
				{
					player.set(\rate, rate);
				}
			);
		});

		gainKnob = Knob()
		.value_(gain)
		.action_({
			|knob|
			gain = knob.value;
			if (player.isPlaying,
				{
					player.set(\amp, gain);
				}
			);
		});

		buttonsView = CompositeView(mainView, Rect(0, 0, width, btn_height));

		buttonsView.layout = HLayout(
			openButton,
			playButton,
			loopTrigger,
			VLayout(
				HLayout(
					rateKnob,
					StaticText()
					.string_("rate")
					.align_(\center),
				)
			),
			VLayout(
				HLayout(
					gainKnob,
					StaticText()
					.string_("gain")
					.align_(\center)
				)
			)
		);

		view = mainView;

		^this;
	}

	play {
		player = Synth(\player,
			[
				\buf, buffer,
				\loop, loop,
				\rate, rate,
				\amp, gain
			]
		);
		player.register;
	}
}