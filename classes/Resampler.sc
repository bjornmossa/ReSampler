Resampler {
  var <>samplers;

  *new {
    | path |
    ^super.new.init(path);
  }

  init {
    | path |
    var window;
    var controlsView;
    var recordPath;
    var pathButton;
    var recordButton;
    var syncButton;
    var server;
    var meter;

    server = Server.local;
    recordPath = path;
    // add synthdef

    SynthDef(\player, {
      |out = 0, buf, rate = 1, start = 0, loop = 0, amp = 1|
      var sig;
      sig = PlayBuf.ar(2, buf, rate, 1, start, loop, 2);
      Out.ar(out, sig * amp);
    }).add;

    // create GUI
    pathButton = Button().states_([
      ["folder", ResamplerColors.white, ResamplerColors.backgroundLight]
    ]).action_({
	  this.changeRecordPath();
    });

    recordButton = Button().states_([
      ["rec", ResamplerColors.white, ResamplerColors.backgroundLight],
      ["stop", ResamplerColors.white, ResamplerColors.backgroundLight]
    ]).action_({
      |button|
	  this.record(button.value);
    });

    syncButton = Button().states_([
      ["sync", ResamplerColors.white, ResamplerColors.backgroundLight],
    ]).action_({
      this.playAll();
    });

    window = Window("SampleTransformer", Rect(0, 0, 1000, 550));
    window.background = ResamplerColors.background;

    meter = ServerMeterView(server, window, 0@0, 0, 2);
    samplers = Array.fill(4, {RSampler.new()});

    window.layout = HLayout(
      GridLayout.rows(
        [samplers[0].view, samplers[1].view],
        [samplers[2].view, samplers[3].view]
      ),
      [
        VLayout(
          pathButton,
          recordButton,
          syncButton,
          meter.view
        ),
        align: \center
      ]
    );
    window.front;

    ^this;
  }

  playAll {
	samplers.do({
	  |sampler|
	  sampler.play;
	});
  }

  changeRecordPath {
	FileDialog(
	  {
		|dir|
		recordPath = dir[0]++"/";
	  },
	  {},
	  2,
	  0
	);
  }

  record {
	|state|
	var filename, path;
	if (state == 1,
	  {
		// create a file
		filename = Date.getDate.stamp++".aiff";
		path = recordPath++filename;
		SoundFile.openWrite(path.standardizePath);
		server.record(path);
	  },
	  {
		server.stopRecording;
	  }
	)
  }
}