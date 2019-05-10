ReSamplerUnit {
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
      ReSamplerColors.yellow,
      ReSamplerColors.yellow
    ];

    sfView.rmsColor = ReSamplerColors.red;
    sfView.gridOn = false;

    sfView.background = ReSamplerColors.backgroundLight;

    openButton = Button()
    .states_([
      ["select file", ReSamplerColors.white, ReSamplerColors.backgroundLight]
    ])
    .action_({
      this.openFile();
    });

    playButton = Button()
    .states_([
      ["play", ReSamplerColors.white, ReSamplerColors.backgroundLight]
    ]).action_({
      this.prStartPlaying();
    });

    loopTrigger = CheckBox()
    .string_("loop")
    .action_({
      |checkbox|
      this.prSetLooping(checkbox.value);
    });

    rateKnob = Knob()
    .color_([
	  ReSamplerColors.background,
	  ReSamplerColors.background,
	  ReSamplerColors.white,
	  ReSamplerColors.white
	])
    .value_(rate)
	.centered_(true)
    .action_({
      |knob|
      this.prSetRate(knob.value.linlin(0.0, 1.0, -1, 2));
    });

    gainKnob = Knob()
	.color_([
	  ReSamplerColors.background,
	  ReSamplerColors.background,
	  ReSamplerColors.white,
	  ReSamplerColors.white
	])
    .value_(gain)
    .action_({
      |knob|
	  this.prSetGain(knob.value);
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

  prStartPlaying {
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

  startPlaying {
    playButton.valueAction_();
  }

  prSetLooping {
    |bool|
    if (bool.value,
      {loop = true},
      {
        loop = false;
        if (player.isPlaying,
          {
            player.set(\loop, 0);
			player.free;
          }
        );
      },
    );
  }

  setLooping {
    |bool|
    loopTrigger.valueAction_(bool);
  }

  prSetRate {
    |value|
    rate = value;
    if (player.isPlaying,
      {
        player.set(\rate, value);
      }
    );
  }
  
  setRate {
    |float|
	rateKnob.valueAction_(float);
  }

  prSetGain {
    |value|
    if (player.isPlaying,
      {
        player.set(\amp, value);
      }
    );
  }

  setGain {
	|float|
	gainKnob.valueAction_(float);
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