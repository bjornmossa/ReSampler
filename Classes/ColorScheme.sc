ResamplerColors {
  classvar <background;
  classvar <backgroundLight;
  classvar <red;
  classvar <green;
  classvar <yellow;
  classvar <white;

  *initClass {
	background = Color.fromHexString("#2e3440");
	backgroundLight = Color.fromHexString("#3b4252");
	red = Color.fromHexString("#bf616a");
    green = Color.fromHexString("#a3be8c");
    yellow = Color.fromHexString("#ebcb8b");
    white = Color.fromHexString("#e5e9f0");
  }
}