ReSampler {
  var <samplers;
  var server;
  var <>recordPath;

  *new {
    | path |
    ^super.new.init(path);
  }

  init {
    | path |
    var window;
    var controlsView;
    var pathButton;
    var recordButton;
    var syncButton;
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
      ["folder", ReSamplerColors.white, ReSamplerColors.backgroundLight]
    ]).action_({
	  this.changeRecordPath();
    });

    recordButton = Button().states_([
      ["rec", ReSamplerColors.white, ReSamplerColors.backgroundLight],
      ["stop", ReSamplerColors.white, ReSamplerColors.backgroundLight]
    ]).action_({
      |button|
	  this.record(button.value);
    });

    syncButton = Button().states_([
      ["sync", ReSamplerColors.white, ReSamplerColors.backgroundLight],
    ]).action_({
      this.playAll();
    });

    window = Window("SampleTransformer", Rect(0, 0, 1000, 550));
    window.background = ReSamplerColors.background;

    meter = ServerMeterView(server, window, 0@0, 0, 2);
    samplers = Array.fill(4, {ReSamplerUnit.new()});

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