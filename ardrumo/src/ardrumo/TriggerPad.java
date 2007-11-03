/*
 * TriggerPad.java
 *
 * Created on October 14, 2007, 10:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ardrumo;

import de.humatic.mmj.MidiOutput;
import de.humatic.mmj.MidiSystem;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mschaff
 */
public class TriggerPad {
	private static int MAXPADVAL = 1023;
	private static int MAXMIDIVAL = 127;
	private static int RAWMINTHRESHOLD = 3;
	
	int padnum;
	String padsound;
	int padnote;
	boolean enabled = true;
	Color padcolor;
	Map<String,Integer> noteMap = new HashMap<String,Integer>();
	MidiOutput mo = null;
	PadPanel pp = null;
	Long lasttime = null;
	Integer lastvel = null;
	int gain = 0;
	
	public TriggerPad(int pnum, String snd, MidiOutput mo) {
		
		this.padnum = pnum;
		this.mo = mo;
		
		initPadNotes();
		
		String[] sndoptions = (String[]) noteMap.keySet().toArray(new String[0]);
		Arrays.sort(sndoptions, String.CASE_INSENSITIVE_ORDER);
		
		pp = new PadPanel(this);
		pp.setSoundCombobox(sndoptions);
		
		if(noteMap.containsKey(snd)) {
			padnote = noteMap.get(snd);
			pp.setSelectedSound(snd);
		}
		
		gain = pp.getGain();
	}
	
	private void initPadNotes() {		
		noteMap.put("Bass Drum, Acoustic",35);
		noteMap.put("Bass Drum 1",36);
		noteMap.put("Side Stick",37);
		noteMap.put("Snare, Acoustic",38);
		noteMap.put("Snare, Roll/Hand Clap",39);
		noteMap.put("Snare, Electric",40);
		noteMap.put("Tom, Low Floor",41);
		noteMap.put("Hi-Hat, Closed",42);
		noteMap.put("Tom, High Floor",43);
		noteMap.put("Hi-Hat, Pedal",44);
		noteMap.put("Tom, Low",45);
		noteMap.put("Hi-Hat, Open",46);
		noteMap.put("Tom, Low-Mid",47);
		noteMap.put("Tom, Hi-Mid",48);
		noteMap.put("Cymbal, Crash 1",49);
		noteMap.put("Tom, High",50);
		noteMap.put("Cymbal, Ride 1",51);
		noteMap.put("Cymbal, Chinese",52);
		noteMap.put("Ride Bell",53);
		noteMap.put("Tambourine",54);
		noteMap.put("Cymbal, Splash",55);
		noteMap.put("Cowbell",56);
		noteMap.put("Cymbal, Crash 2",57);
		noteMap.put("Vibraslap",58);
		noteMap.put("Cymbal, Ride 2",59);
		noteMap.put("Bongo, Hi",60);
		noteMap.put("Bongo, Low",61);
		noteMap.put("Conga, Mute Hi",62);
		noteMap.put("Conga, Open Hi",63);
		noteMap.put("Conga, Low",64);
		noteMap.put("Timbale, High",65);
		noteMap.put("Timbale, Low",66);
		noteMap.put("Agogo, High",67);
		noteMap.put("Agogo, Low",68);
		noteMap.put("Cabasa",69);
		noteMap.put("Maracas",70);
		noteMap.put("Whistle, Short",71);
		noteMap.put("Whistle, Long",72);
		noteMap.put("Guiro, Short",73);
		noteMap.put("Guiro, Long",74);
		noteMap.put("Claves",75);
		noteMap.put("Wood Block, Hi",76);
		noteMap.put("Wood Block, Low",77);
		noteMap.put("Cuica, Mute",78);
		noteMap.put("Cuica, Open",79);
		noteMap.put("Triangle, Mute",80);
		noteMap.put("Triangle, Open",81);
		noteMap.put("Jingle Bells",83);
		noteMap.put("Chimes",84);
		noteMap.put("Gong",85);
	}
	
	public void setPadnum(int p) {
		padnum = p;
	}
	
	public void setEnabled(boolean b) {
		enabled = b;
	}
		
	public void setPadsound(String s) {
		if(noteMap.containsKey(s)) {
			padnote = noteMap.get(s);
			padsound = s;
		}
	}
	
	public void setMidiOutput(MidiOutput m) {
		mo = m;
	}
	
	public void getPadnote(int n) {
		padnote = n;
	}
	
	public void playNote(int vel) {
		if(enabled) {
			if (mo != null) {
				try { 
					mo.sendMidi(new byte[] { -112, (byte)padnote, (byte)vel }, MidiSystem.getHostTime());
					//Thread.sleep(vel);
					mo.sendMidi(new byte[] { -128, (byte)padnote, 0 }, MidiSystem.getHostTime());
					
				} catch(Exception me) {
				}
			}
		}
	}
	
	public int getPadnum() {
		return padnum;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public String getPadsound() {
		return padsound;
	}
	
	public int getPadnote() {
		return padnote;
	}
	
	public PadPanel	getPadPanel() {
		return pp;
	}
	
	public void setGain(int g) {
		gain = g;
	}
	
	public int getGain() {
		return gain;
	}
	
	public void sendData(int dat) {
		// scale the data from ()-1023 to a MIDI velocity of 0 to 127
		int vel = (new Double( (dat + gain) * 127 / 1024)).intValue();
		
		if(vel > MAXMIDIVAL) {
			vel = MAXMIDIVAL;
		}
		
		long nowtime = MidiSystem.getHostTime(); 
		
		if (lasttime == null) {
			lasttime = new Long(nowtime);
			lastvel = new Integer(vel);
		} else {
			if( ( vel > lastvel || nowtime > (lasttime + 60000)) && dat > RAWMINTHRESHOLD && vel > 0) {
				pp.setLEDBlink();
				System.out.println(nowtime + " : " + padnum + " : " + padnote + " : "+dat + " : scaled="+vel);
				playNote(vel);
				
				lasttime = new Long(nowtime);
				lastvel = new Integer(vel);
			}
		}
	}	
}
