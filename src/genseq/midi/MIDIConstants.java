/**
 * MIDIConstants
 * 
 * This interface contains constants that will be commonly
 * used by any number of classes in the GenSeq package.
 *
 * This constants define pitches, instruments, and other MIDI
 * information.
 *
 */

package genseq.midi;

public interface MIDIConstants {

	// MIDI Pitches
	public static final int C0 = 0;
	public static final int CS0 = 1;
	public static final int D0 = 2;
	public static final int DS0 = 3;
	public static final int E0 = 4;
	public static final int F0 = 5;
	public static final int FS0 = 6;
	public static final int G0 = 7;
	public static final int GS0 = 8;
	public static final int A0 = 9;
	public static final int AS0 = 10;
	public static final int B0 = 11;

	public static final int C1 = 12;
	public static final int CS1 = 13;
	public static final int D1 = 14;
	public static final int DS1 = 15;
	public static final int E1 = 16;
	public static final int F1 = 17;
	public static final int FS1 = 18;
	public static final int G1 = 19;
	public static final int GS1 = 20;
	public static final int A1 = 21;
	public static final int AS1 = 22;
	public static final int B1 = 23;

	public static final int C2 = 24;
	public static final int CS2 = 25;
	public static final int D2 = 26;
	public static final int DS2 = 27;
	public static final int E2 = 28;
	public static final int F2 = 29;
	public static final int FS2 = 30;
	public static final int G2 = 31;
	public static final int GS2 = 32;
	public static final int A2 = 33;
	public static final int AS2 = 34;
	public static final int B2 = 35;

	public static final int C3 = 36;
	public static final int CS3 = 37;
	public static final int D3 = 38;
	public static final int DS3 = 39;
	public static final int E3 = 40;
	public static final int F3 = 41;
	public static final int FS3 = 42;
	public static final int G3 = 43;
	public static final int GS3 = 44;
	public static final int A3 = 45;
	public static final int AS3 = 46;
	public static final int B3 = 47;

	public static final int C4 = 48;
	public static final int CS4 = 49;
	public static final int D4 = 50;
	public static final int DS4 = 51;
	public static final int E4 = 52;
	public static final int F4 = 53;
	public static final int FS4 = 54;
	public static final int G4 = 55;
	public static final int GS4 = 56;
	public static final int A4 = 57;
	public static final int AS4 = 58;
	public static final int B4 = 59;

	public static final int C5 = 60;
	public static final int CS5 = 61;
	public static final int D5 = 62;
	public static final int DS5 = 63;
	public static final int E5 = 64;
	public static final int F5 = 65;
	public static final int FS5 = 66;
	public static final int G5 = 67;
	public static final int GS5 = 68;
	public static final int A5 = 69;
	public static final int AS5 = 70;
	public static final int B5 = 71;

	public static final int C6 = 72;
	public static final int CS6 = 73;
	public static final int D6 = 74;
	public static final int DS6 = 75;
	public static final int E6 = 76;
	public static final int F6 = 77;
	public static final int FS6 = 78;
	public static final int G6 = 79;
	public static final int GS6 = 80;
	public static final int A6 = 81;
	public static final int AS6 = 82;
	public static final int B6 = 83;

	public static final int C7 = 84;
	public static final int CS7 = 85;
	public static final int D7 = 86;
	public static final int DS7 = 87;
	public static final int E7 = 88;
	public static final int F7 = 89;
	public static final int FS7 = 90;
	public static final int G7 = 91;
	public static final int GS7 = 92;
	public static final int A7 = 93;
	public static final int AS7 = 94;
	public static final int B7 = 95;

	public static final int C8 = 96;
	public static final int CS8 = 97;
	public static final int D8 = 98;
	public static final int DS8 = 99;
	public static final int E8 = 100;
	public static final int F8 = 101;
	public static final int FS8 = 102;
	public static final int G8 = 103;
	public static final int GS8 = 104;
	public static final int A8 = 105;
	public static final int AS8 = 106;
	public static final int B8 = 107;

	public static final int C9 = 108;
	public static final int CS9 = 109;
	public static final int D9 = 110;
	public static final int DS9 = 111;
	public static final int E9 = 112;
	public static final int F9 = 113;
	public static final int FS9 = 114;
	public static final int G9 = 115;
	public static final int GS9 = 116;
	public static final int A9 = 117;
	public static final int AS9 = 118;
	public static final int B9 = 119;

	public static final int C10 = 120;
	public static final int CS10 = 121;
	public static final int D10 = 122;
	public static final int DS10 = 123;
	public static final int E10 = 124;
	public static final int F10 = 125;
	public static final int FS10 = 126;
	public static final int G10 = 127;

	// Rest
	public static final int REST = 1000;

	// Note names, for converting ints to strings
	public static final String[] PITCHES = {
			"REST --- rest",
			"C0",
			"C#0",
			"D0",
			"D#0",
			"E0",
			"F0",
			"F#0",
			"G0",
			"G#0",
			"A0",
			"A#0",
			"B0",
			"C1",
			"C#1",
			"D1",
			"D#1",
			"E1",
			"F1",
			"F#1",
			"G1",
			"G#1",
			"A1",
			"A#1",
			"B1",
			"C2",
			"C#2",
			"D2",
			"D#2",
			"E2",
			"F2",
			"F#2",
			"G2",
			"G#2",
			"A2",
			"A#2",
			"B2",
			"C3 --- \"Low C\"",
			"C#3",
			"D3",
			"D#3",
			"E3",
			"F3",
			"F#3",
			"G3",
			"G#3",
			"A3",
			"A#3",
			"B3",
			"C4",
			"C#4",
			"D4",
			"D#4",
			"E4",
			"F4",
			"F#4",
			"G4",
			"G#4",
			"A4 --- \"A 440Hz\"",
			"A#4",
			"B4",
			"C5 --- \"Middle C\"",
			"C#5",
			"D5",
			"D#5",
			"E5",
			"F5",
			"F#5",
			"G5",
			"G#5",
			"A5",
			"A#5",
			"B5",
			"C6",
			"C#6",
			"D6",
			"D#6",
			"E6",
			"F6",
			"F#6",
			"G6",
			"G#6",
			"A6",
			"A#6",
			"B6",
			"C7 --- \"High C\"",
			"C#7",
			"D7",
			"D#7",
			"E7",
			"F7",
			"F#7",
			"G7",
			"G#7",
			"A7",
			"A#7",
			"B7",
			"C8",
			"C#8",
			"D8",
			"D#8",
			"E8",
			"F8",
			"F#8",
			"G8",
			"G#8",
			"A8",
			"A#8",
			"B8",
			"C9",
			"C#9",
			"D9",
			"D#9",
			"E9",
			"F9",
			"F#9",
			"G9",
			"G#9",
			"A9",
			"A#9",
			"B9",
			"C10",
			"C#10",
			"D10",
			"D#10",
			"E10",
			"F10",
			"F#10",
			"G10"
	};

}
