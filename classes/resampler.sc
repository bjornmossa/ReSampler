Resampler {
	*new {
		^super.new.init();
	}

	init {
		var window;
		var samplesView;
		var controlsView;
		var recordPath;
		var pathButton;
		var recordButton;
		var syncButton;
		var server;
		var samplers;
		var meter;
		var meterView;

		server = Server.local;
		recordPath = PathName(thisProcess.nowExecutingPath).parentPath;

		// add synthdef

		SynthDef(\player, {
			|out = 0, buf, rate = 1, start = 0, loop = 0, amp = 1|
			var sig;
			sig = PlayBuf.ar(2, buf, rate, 1, start, loop, 2);
			Out.ar(out, sig * amp);
		}).add;

		// create GUI

		pathButton = Button().states_([
			["folder"]
		]).action_({
			FileDialog(
				{
					|dir|
					recordPath = dir[0]++"/";
				},
				{},
				2,
				0
			);
		});

		recordButton = Button().states_([
			["rec"],
			["stop"]
		]).action_({
			|button|
			var filename, path;
			if (button.value == 1,
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
		});

		syncButton = Button().states_([
			["sync"],
		]).action_({
			samplers.do({|sampler| sampler.play;});
		});

		window = Window("SampleTransformer", Rect(0, 0, 1000, 550));

		samplesView = CompositeView(window, Rect(0, 0, 940, 550));

		// add samplers
		samplers = Array.new(4);

		// can't undestand why Array.fill don't work

		samplers.add(Sampler.new(samplesView, 0, 0));
		samplers.add(Sampler.new(samplesView, 0, 0));
		samplers.add(Sampler.new(samplesView, 0, 0));
		samplers.add(Sampler.new(samplesView, 0, 0));

		samplesView.layout = HLayout(
			VLayout(
				samplers[0].view,
				samplers[1].view,
			),
			VLayout(
				samplers[2].view,
				samplers[3].view,
			)
		);

		controlsView = CompositeView(window, Rect(950, 0, 60, 100));
		meterView = CompositeView(window, Rect(940, 100, 60, 450));

		meter = ServerMeterView.new(server, meterView, 0@0, 0, 2);

		controlsView.layout = VLayout(
			pathButton,
			recordButton,
			syncButton
		);

		window.front;

		^this;
	}
}