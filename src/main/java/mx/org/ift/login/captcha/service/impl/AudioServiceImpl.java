package mx.org.ift.login.captcha.service.impl;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

import mx.org.ift.login.captcha.constants.Constantes;
import mx.org.ift.login.captcha.service.AudioService;

import javax.sound.sampled.Clip;


public class AudioServiceImpl implements AudioService{
	private SynthesizerModeDesc desc;
	private Synthesizer synthesizer;
	
	public String translate2(String texto){
		String u = new ImageServiceImpl().decrypt(Constantes.TR) + texto;
		try {
			InputStream is = new URL(u).openStream();
			
			BufferedInputStream bis = new BufferedInputStream( is );
			
	        AudioInputStream in = AudioSystem.getAudioInputStream(bis);
	        
	        
	        DataLine.Info info = new DataLine.Info(Clip.class, in.getFormat());
	        
	        Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(in);
            clip.addLineListener(new LineListener()
            {
                public void update(LineEvent event)
                {
                    if (event.getType() == LineEvent.Type.STOP)
                    {
                        event.getLine().close();
                    }
                }
            });
            clip.start();
            
			/*AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                false);
			 din = AudioSystem.getAudioInputStream(decodedFormat, in);
		     DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
		     SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
		     if(line != null) {
		    	 line.open(decodedFormat);
		         byte[] data = new byte[4096];
		         // Start
		         line.start();

		         int nBytesRead;
		         while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
		        	 line.write(data, 0, nBytesRead);
		         }
		         // Stop
		         line.drain();
		         line.stop();
		         line.close();
		         din.close();
		     }*/
		     in.close();
		     is.close();
		}catch(MalformedURLException err){
			System.err.println("A:" + err.getMessage());
		}catch(IOException err){
			System.err.println("B:" + err.getMessage());
		}catch(LineUnavailableException err){
			System.err.println("C:" + err.getMessage());
		}catch(UnsupportedAudioFileException err){
			System.err.println("D:" + err.getMessage());
		}
		return u;
	}
	
	public String translate(String texto){
		String u = Constantes.TR + texto;
		try {
			URL url = new URL(u);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    
			conn.setRequestMethod("GET");

			conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "text/plain");
            conn.addRequestProperty("User-Agent", "Mozilla/4.0");
            conn.setRequestProperty("Content-Type", "audio/mpeg");
            conn.setRequestProperty("charset", "UTF-8");
            
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				u += output;
			}
			br.close();
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return u;
	}
	
	public void init(String voiceName) 
		    throws EngineException, AudioException, EngineStateError, 
		           PropertyVetoException {
		if (desc == null) {
		      
			System.setProperty("freetts.voices", 
		        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		      
			//Locale locale = new Locale ( "es" , "ES" );
		    desc = new SynthesizerModeDesc(Locale.US);
		    Central.registerEngineCentral
		        ("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
		    synthesizer = Central.createSynthesizer(desc);
		    synthesizer.allocate();
		    synthesizer.resume();
		    SynthesizerModeDesc smd = 
		        (SynthesizerModeDesc)synthesizer.getEngineModeDesc();
		    Voice[] voices = smd.getVoices();
		    Voice voice = null;
		    for(int i = 0; i < voices.length; i++) {
		    	System.out.println(voices[i].getName());
		    	if(voices[i].getName().equals(voiceName)) {
		    		voice = voices[i];
		    		System.out.println(voices[i].getName());
		    		break;
		        }
		    }
		    synthesizer.getSynthesizerProperties().setVoice(voice);
		}
		    
	}
	
	public void terminate() throws EngineException, EngineStateError {
	    synthesizer.deallocate();
	}
	  
	public void doSpeak(String speakText) 
	    throws EngineException, AudioException, IllegalArgumentException, 
	           InterruptedException {
	      synthesizer.speakPlainText(speakText, null);
	      synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);

	}
	
	
}
