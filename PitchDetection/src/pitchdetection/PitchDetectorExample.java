/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pitchdetection;

/**
 *
 * @author Chachi.Desuasido
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import javax.sound.sampled.AudioSystem;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class PitchDetectorExample extends JFrame implements PitchDetectionHandler {
     JTextField text = new JTextField();
     JLabel img = new JLabel();
	 JLabel bg = new JLabel();
	JLabel pipeB = new JLabel();
	JLabel pipeT = new JLabel();

	/**
	 *
	 */

	private static final long serialVersionUID = 3501426880288136245L;

	private AudioDispatcher dispatcher;
	private Mixer currentMixer;


	final float timestep =1.f/60.f;

	final float gravityX=0.f;
	final float gravityY=9.8f;
	float posX=0.f;
	float posY=0.f;

	float velX=0.f;
	float velY=0.f;

	float pipeX=0;
	float pipeY=0;




	private PitchEstimationAlgorithm algo;
     public void jump(){
     	velY=-25.f;


     }






	public PitchDetectorExample() {
        super("pitch");
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setLayout(null);
        super.setLocationRelativeTo(null);

		ImageIcon backgroundIcon = new ImageIcon("res/background-simple.png");

		//background = backgroundIcon.getImage();



        super.add(text);
        text.setSize(60, 24);
        text.setLocation(700, 500);



		super.add(pipeT);
		pipeT.setSize(29,250);
		pipeT.setLocation(280,0);
		pipeT.setIcon(new ImageIcon("res/tt.png"));



		super.add(pipeB);
		pipeB.setSize(29,250);
		pipeB.setLocation(280,320);
		pipeB.setIcon(new ImageIcon("res/bt.png"));






     super.add(img);
		img.setSize(50,50);
		img.setLocation(0,0);
		img.setIcon(new ImageIcon("res/android.png"));




		super.add(bg);
		bg.setIcon(new ImageIcon("res/bg1.png"));
		bg.setSize(338,600);


        text.setHorizontalAlignment(JTextField.CENTER);

		algo = PitchEstimationAlgorithm.FFT_YIN;
                try {
                    for(Mixer.Info info : Shared.getMixerInfo(false, true)){
                        if(info.getName().contains("Microphone")) {
                            setNewMixer(AudioSystem.getMixer(info));
                            break;
                        }
                    }
                } catch(Exception e) {

                }
        new Thread(new Runnable() {
        	long lastUpdate=0;
			@Override
			public void run() {
				while(true) {


					float delta=(System.nanoTime()-lastUpdate)/1000000.f;
					if(delta>timestep)delta=timestep;
					//System.out.println(delta);

					velX+=gravityX*delta;
					velY+=gravityY*delta;

					PitchDetectorExample.this.posX+=velX*delta;
					PitchDetectorExample.this.posY+=velY*delta;


					if(posY>480.f){
						velY=0.f;
						posY=480.f;


					}
					img.setLocation((int)posX,(int)posY);

					try{
						Thread.sleep(1);
						img.setIcon(new ImageIcon("res/android2.png"));
					}catch (Exception e ){

					}

				}
			}
		}).start();
                new Thread(new Runnable() {
					@Override
					public void run() {
					while(true) {
						pipeX -= 1;

						if (pipeB.getX()<-100){
							pipeB.setLocation(pipeB.getX()+500,350*(1+(int)Math.random()*50));
							pipeT.setLocation(pipeT.getX() + 500, -20*(1+(int)Math.random()*50));

						}
						pipeB.setLocation(pipeB.getX() + (int)pipeX, 350);
						pipeT.setLocation(pipeT.getX() + (int)pipeX, -20);
						try{
							Thread.sleep(100);
							img.setIcon(new ImageIcon("res/android2.png"));
						}catch (Exception e ){

						}
					}
					}

				}).start();



        }





	private void setNewMixer(Mixer mixer) throws LineUnavailableException,
			UnsupportedAudioFileException {

		if(dispatcher!= null){
			dispatcher.stop();
		}
		currentMixer = mixer;

		float sampleRate = 44100;
		int bufferSize = 1024;
		int overlap = 0;

		final AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
				true);
		final DataLine.Info dataLineInfo = new DataLine.Info(
				TargetDataLine.class, format);
		TargetDataLine line;
		line = (TargetDataLine) mixer.getLine(dataLineInfo);
		final int numberOfSamples = bufferSize;
		line.open(format, numberOfSamples);
		line.start();
		final AudioInputStream stream = new AudioInputStream(line);

		JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
		// create a new dispatcher
		dispatcher = new AudioDispatcher(audioStream, bufferSize,
				overlap);

		// add a processor
		dispatcher.addAudioProcessor(new PitchProcessor(algo, sampleRate, bufferSize, this));

		new Thread(dispatcher,"Audio dispatching").start();
	}

	public static void main(String... strings) throws InterruptedException{

				JFrame frame = new PitchDetectorExample();
                                frame.setSize(338,600);
				frame.setVisible(true);


	}


	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
		if(pitchDetectionResult.getPitch() != -1){
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();
			double rms = audioEvent.getRMS() * 100;
                        System.out.println(String.valueOf(pitch));
                         text.setText(String.valueOf(pitchDetectionResult.getPitch()));
                         if(pitchDetectionResult.getPitch()>100.f){
							 img.setIcon(new ImageIcon("res/android1.png"));
                            jump();

                         }

                }
        }
}
