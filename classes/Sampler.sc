RSampler {
  var path = nil;
  var soundFile = nil;
  var sfView = nil;
  var player = nil;
  var loop = false;
  var rate = 1;
  var gain = 1;
  var openButton = nil;
  var rateKnob = nil;
  var gainKnob = nil;
  var playButton = nil;
  var loopTrigger = nil;
  var buffer = nil;
  var <>view;

  *new {
    ^super.new.init();
  }

  init {
    soundFile = SoundFile();
    sfView = SoundFileView();
    sfView.elasticMode = true;
    sfView.waveColors = [
      ResamplerColors.yellow,
      ResamplerColors.yellow
    ];

    sfView.rmsColor = ResamplerColors.red;
    sfView.gridOn = false;

    sfView.background = ResamplerColors.backgroundLight;

    openButton = Button()
    .states_([
      ["select file", ResamplerColors.white, ResamplerColors.backgroundLight]
    ])
    .action_({
      this.openFile();
    });

    playButton = Button()
    .states_([
      ["play", ResamplerColors.white, ResamplerColors.backgroundLight]
    ]).action_({
      this.startPlaying();
    });

    loopTrigger = CheckBox()
    .string_("loop")
    .action_({
      |checkbox|
      this.setLooping(checkbox.value);
    });

    rateKnob = Knob()
    .color_([
	  ResamplerColors.background,
	  ResamplerColors.background,
	  ResamplerColors.white,
	  ResamplerColors.white
	])
    .value_(rate)
    .action_({
      |knob|
      this.setRate(knob.value.linlin(0.0, 1.0, -1, 1));
    });

    gainKnob = Knob()
	.color_([
	  ResamplerColors.background,
	  ResamplerColors.background,
	  ResamplerColors.white,
	  ResamplerColors.white
	])
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

    view = VLayout(
      HLayout(
        openButton,
        playButton,
        loopTrigger,
        rateKnob,
        gainKnob
      ),
      [sfView, stretch: 1]
    );

    ^this;
  }

  openFile {
    FileDialog(
      {
        |file|
        var filename, outpath;
        path = file[0];
        filename = PathName(path).fileName;
        outpath = PathName.tmp++filename;

        soundFile.openRead(path);
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
  }

  startPlaying {
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
  }

  setLooping {
    |bool|
    if (bool.value,
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
  }

  setRate {
    |value|
    rate = value;
    if (player.isPlaying,
      {
        player.set(\rate, value);
      }
    );
  }

  setGain {
    |value|
    if (player.isPlaying,
      {
        player.set(\amp, value);
      }
    );
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

  stop {
    if (player.isPlaying,
      {
        player.free;
      }
    );
  }
}