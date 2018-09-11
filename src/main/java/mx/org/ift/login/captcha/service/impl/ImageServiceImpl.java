package mx.org.ift.login.captcha.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mx.org.ift.login.captcha.constants.Constantes;
import mx.org.ift.login.captcha.service.ImageService;
import mx.org.ift.login.captcha.vo.OperacionVO;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static mx.org.ift.login.captcha.constants.Constantes.ALGO;

public class ImageServiceImpl implements ImageService{
	private final static Logger LOGGER = 
			Logger.getLogger("mx.org.ift.login.captcha.service.impl.ImageServiceImpl");
	
	public OperacionVO operacion(){
		OperacionVO vo = new OperacionVO();
		String op_ = "";
		Boolean repetir = false;
		
		int op = 1;
		int a = 1;
		int b = 1;
		int r = 0;
		do{
			repetir = false;
			do{
				a = new Random().nextInt(10);
			}while(a < 1);
			
			do{
				b = new Random().nextInt(10);
			}while(b < 1);
			
			do{
				op = new Random().nextInt(4);
			}while(op < 1);
			
			switch(op){
			case 1:
				op_ = " mas "; 		
				r = a + b;
				break;
			case 2:
				op_ = " menos "; 
				r = a - b;
				if(r < 0){
					repetir = true;
				}
				break;
			case 3:
				op_ = " por "; 
				r = a * b;
				break;
			case 4:
				op_ = " entre "; 
				double r1 = (double)a / b;
				long c = (long)r1;
				double d = r1 - c;
				//System.out.println(r + " " + c + " " + d);
				if(a < b || d != 0){
					repetir = true;
				}
				r = a/b;
				break;
			}
		}while(repetir);
		vo.setOperacion(a + op_ + b);
		vo.setResultado(r);
		return vo;
	}
	
	public byte[] text2image(String text){
		byte[] imageInByte = null;
		String t1 = text;
    	String t2 = "";
    	String temp = "9 operacion 9";
    	int factor = 3;
		int pos = text.indexOf("|");
		
		if(pos > 0){
        	t1 = text.substring(0, pos);
        	t2 = text.substring(pos+1, text.length());
        	factor = 3;
        } 
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toDegrees(25), 0, 0);
		Font rotatedFont = font.deriveFont(affineTransform);
        g2d.setFont(rotatedFont);
        FontMetrics fm = g2d.getFontMetrics();
        
        int width = fm.stringWidth(temp);
        int height = fm.getHeight() * factor;
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        
        int y = 20;
        int x = 15;
        if(pos > 0){
            
        	y = 10;
        	g2d.drawString(t1, x, y += g2d.getFontMetrics().getHeight());
        	
        	y += 20;
        	g2d.drawString(t2, x, y += g2d.getFontMetrics().getHeight());
        	
        } else {
            
        	g2d.drawString(text, x, y += fm.getAscent());
        }
        g2d.dispose();
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos); //new File("c:/temp/images/Text.jpg"));
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

		return imageInByte;
	}
	
	public String encrypt(String data){
		byte[] encVal = null;
		Key key = generateKey();
        try{
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        encVal = c.doFinal(data.getBytes());
        }catch(NoSuchAlgorithmException err){
        	
        }catch(NoSuchPaddingException err){
        	
        }catch(InvalidKeyException err){
        	
        }catch(BadPaddingException err){
        	
        }catch(IllegalBlockSizeException err){
        	
        }
        return Base64.getEncoder().encodeToString(encVal);
        
    }

	/**
     * Generate a new encryption key.
     */
    private Key generateKey()  {
        return new SecretKeySpec(new Constantes().keyValue, ALGO);
    }
    
    /**
     * Decrypt a string with AES algorithm.
     *
     * @param encryptedData is a string
     * @return the decrypted string
     */
    public String decrypt(String encryptedData) {
    	byte[] decValue = null;
        Key key = generateKey();
        try{
	        Cipher c = Cipher.getInstance(ALGO);
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
	        decValue = c.doFinal(decordedValue);
        }catch(NoSuchAlgorithmException err){
        	
        }catch(NoSuchPaddingException err){
        	
        }catch(InvalidKeyException err){
        	
        }catch(BadPaddingException err){
        	
        }catch(IllegalBlockSizeException err){
        	
        }
        return new String(decValue);
    }
    
    public String map2JSON(OperacionVO op, OperacionVO op2, String legend, Integer it){
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put( "op1", op.getOperacion() );
	    map.put( "re1", op.getResultado() );
	    if(op2 != null){
	    	map.put( "op2", op2.getOperacion() );
	    	map.put( "re2", op2.getResultado() );
	    }
	    map.put( "leg", legend );
	    map.put( "it", it );
	    
	    JSONObject json = new JSONObject(map);
	    
	    //System.out.printf( " %s", json.toString() );
	    return json.toString();
	}
	public static void main(String...strings ){
		String alfa = "http://translate.google.com/translate_tts?client=tw-ob&tl=es&q=";
		ImageService is = new ImageServiceImpl();
		
		alfa = is.encrypt(alfa);
		System.out.println(alfa);
		OperacionVO b = is.operacion();
		String a = is.decrypt(Constantes.TR);
		System.out.println(a);
		is.text2image(b.getOperacion());
	}
	
	
}
