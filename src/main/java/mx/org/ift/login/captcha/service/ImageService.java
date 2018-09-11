package mx.org.ift.login.captcha.service;

import mx.org.ift.login.captcha.vo.OperacionVO;


public interface ImageService {
	byte[] text2image(String text);
	String encrypt(String data);
	String decrypt(String encryptedData);
	OperacionVO operacion();
	String map2JSON(OperacionVO op, OperacionVO op2, String legend, Integer it);
}