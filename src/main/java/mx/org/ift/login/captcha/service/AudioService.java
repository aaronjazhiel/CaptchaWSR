package mx.org.ift.login.captcha.service;

import java.beans.PropertyVetoException;

import javax.speech.AudioException;
import javax.speech.EngineException;
import javax.speech.EngineStateError;

public interface AudioService {
	void init(String voiceName)throws EngineException, AudioException, EngineStateError, 
    PropertyVetoException;
	
	public String translate2(String texto);
	String translate(String texto);
	void terminate() throws EngineException, EngineStateError;
	  
	void doSpeak(String speakText) 
	    throws EngineException, AudioException, 
	           InterruptedException;
}
