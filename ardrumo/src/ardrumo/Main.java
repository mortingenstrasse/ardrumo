/*
 * Main.java
 *
 * Created on October 14, 2007, 3:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ardrumo;

import com.sun.org.apache.bcel.internal.verifier.statics.DOUBLE_Upper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import de.humatic.mmj.MidiInput;
import de.humatic.mmj.MidiListener;
import de.humatic.mmj.MidiOutput;
import de.humatic.mmj.MidiSystem;
import de.humatic.mmj.MidiSystemListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author mschaff
 */
public class Main extends JFrame implements SerialPortEventListener {
	private static int SERIALSPEED = 57600;
	SerialPort serport;
	InputStream in;
	OutputStream out;
	String s = "";
	MidiOutput mo;
	
	TriggerPad[] pads = null;
	Map<Integer,String> padMap;
	
	public void init() {
		int padCount = 6;
		final JComboBox[] cb = new JComboBox[padCount];
		padMap = new HashMap<Integer,String>();
		
		setLayout(new BorderLayout());
		SettingsPanel sp = new SettingsPanel(this);
		sp.setSerialOptions(getSerialPorts());
		setSerialPort(sp.getSerialInput());
		
		JPanel ppds = new JPanel();
		ppds.setLayout(new FlowLayout());
		pads = new TriggerPad[padCount];
		
		// Default pad sounds
		
		padMap.put(0,"Hi-Hat, Open");
		padMap.put(1,"Snare, Acoustic");
		padMap.put(2,"Cymbal, Crash 1");
		padMap.put(3,"Tom, Hi-Mid");
		padMap.put(4,"Tom, Low-Mid");
		padMap.put(5,"Bass Drum, Acoustic");
		
		MidiSystem.initMidiSystem("Arduino", "VirtualMIDI");
		String[] md = MidiSystem.getOutputs();
		System.out.print("Ardrumo: MIDI outputs: ");
		System.out.println(Arrays.deepToString(md));
		
		mo = MidiSystem.openMidiOutput(0);
		
		for(int i=0; i<padCount; i++) {
			final int padnum = i;
			
			pads[i] = new TriggerPad(i, 1, padMap.get(padnum), mo);
			ppds.add(pads[i].getPadPanel());
		}
		
		add(sp,BorderLayout.PAGE_START);
		add(ppds,BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		
		
		setLocation( 0,0);
		setSize( 770,  414);
		
		setVisible(true);
	}
	    
	public Main() {
		super();
		init();
	}

	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType()== SerialPortEvent.DATA_AVAILABLE) {
			try {
				while( in.available() > 0) {
					char c = (char)in.read();

					if(c == '\n') {
						StringTokenizer st=new StringTokenizer(s,",");
						String strPad = st.nextToken();
						int pad = Integer.parseInt(strPad.trim());
						String strDat = st.nextToken();
						int dat = Integer.parseInt(strDat.trim());
						s = "";

						pads[pad].sendData(dat);

					} else {
						s += String.valueOf(c);
					}
				}
			} catch (IOException e) {
			}
		} 
	}
	
	public Vector<String> getSerialPorts() {
		Vector<String> vp = new Vector<String>();
		
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier)portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println(portId + ": " + portId.getName() + " " +portId.getCurrentOwner());
				vp.add(portId.getName());
			}
		}
		
		return vp;
	}
	
	public void setSerialPort(String port) {
		int speed = SERIALSPEED;
		
		if(serport != null) {
			serport.close();
		}
		
		//which port you want to use and the baud come in as parameters
		try {
			//find the port
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
			//open the port
			serport = (SerialPort)portId.open("my_java_serial" + port, 2000);
			//configure the port
			try {
				serport.setSerialPortParams(speed,
				serport.DATABITS_8,
				serport.STOPBITS_1,
				serport.PARITY_NONE);
			} catch (UnsupportedCommOperationException e){
				System.out.println("Probably an unsupported Speed");
			}
		
			//establish streams for reading and writing to the port
			try {
				in = serport.getInputStream();
				out = serport.getOutputStream();
			} catch (IOException e) {
				System.out.println("couldn't get streams");
			}
			
			// we could read from "in" in a separate thread, but the API gives us events
			try {
				serport.addEventListener(this);
				serport.notifyOnDataAvailable(true);
			} catch ( TooManyListenersException e ) {
				System.out.println("couldn't add listener");
			}
		
		} catch (Exception e) { 
			System.out.println("Port in Use: "+e);
		}
	}
	
	public static void main(String[] args) {
		Main m = new Main();
	}
	
}
