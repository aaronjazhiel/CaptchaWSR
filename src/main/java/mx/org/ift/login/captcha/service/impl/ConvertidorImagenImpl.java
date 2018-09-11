package mx.org.ift.login.captcha.service.impl;

import java.io.File;
import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import mx.org.ift.login.captcha.vo.OperacionVO;

/**
 * Clase para generar Imagen Captcha.
 * @author Gerardo G. Burguete *
 */
public class ConvertidorImagenImpl  {

	private String DIVISION       = "entre";
	private String RESTA          = "menos";
	
    /**
     * Metodo para crear una imagen de Captcha en string base64
     * @param vo datos de Captcha1
     * @param vo2 datos de captcha2
     * @return cadena base64 con imagen captcha generada.
     * @throws IOException Excepcion en caso de error al generar el captcha
     */
    public byte[] createCaptcha(OperacionVO vo, OperacionVO vo2) {
        byte[] imageInByte = null;
        CaptchaParameters captcha = getCaptchaParameters(vo.getOperando1());
        BufferedImage image = new BufferedImage(captcha.width, captcha.height,
        		                                  BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(Color.WHITE);
        Paint oldPaint = graphics2D.getPaint();
        graphics2D.setPaint(captcha.gradient);
        graphics2D.fillRect(0, 0, captcha.width, captcha.height);
        graphics2D.setPaint(oldPaint);
        
        int iNumX = (captcha.height -1)/30;
        int iNumY = (captcha.width  -1)/30;
        int iCont = 1;
        int iMod  = 0;
        
        for (int i = 0; i <= iNumX; i++) {        	
        	iMod = iCont%2;
        	if(iMod == 1)
        		graphics2D.setColor(Color.YELLOW);
        	else
        		graphics2D.setColor(Color.WHITE);
        	graphics2D.fillRect(0,  (25 + (35 * i)), (captcha.width-1), 3);
        	iCont++;
        }

        iCont = 1;
        iMod  = 0;
        for(int i = 0; i <= iNumY; i++) {
        	iMod = iCont%2;
        	if(iMod == 1){
        		graphics2D.setColor(Color.WHITE);
        	} else {
        		graphics2D.setColor(Color.YELLOW);        	
			}
        	graphics2D.fillRect( (25 + (35 * i)), 0, 3, captcha.height-1 );
        	iCont++;
        }

        
        graphics2D.setColor(Color.WHITE);
        graphics2D.dispose();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try{
	        ImageIO.write(image, "PNG", outputStream);
	        outputStream.flush();
	        imageInByte = outputStream.toByteArray();
	        outputStream.close();
        } catch(IOException err){
        	
        }
        
        int iNumFilas = (vo2 == null ? 1 : 2);
        int iV2 = 0;
        
        int iPrimerOp  = 0;
        int iSegundoOp = 0;
        
        
        if(vo.getOperador().equalsIgnoreCase(DIVISION)){
        	iPrimerOp  = -155;
        	iSegundoOp =  155;
        } else if(vo.getOperador().equalsIgnoreCase(RESTA)){
        	iPrimerOp  = -145;
        	iSegundoOp =   255;
        } else {
        	iPrimerOp  = -115;
        	iSegundoOp =  120;
        }
		
        if (iNumFilas == 1){
        	iV2 = -40;
        } else {
        	iV2 = -80;
        }
		
        imageInByte = agregaElemntoImagen(imageInByte, vo.getOperando1(),
        		                              iPrimerOp , iV2);
        imageInByte = agregaElemntoImagen(imageInByte, vo.getOperador(),
        		                                     -50, iV2);
        imageInByte = agregaElemntoImagen(imageInByte, vo.getOperando2(), 
        		                              iSegundoOp, iV2);
        
        if( iNumFilas == 2) {
            
            iV2 = 50;
            if(vo2.getOperador().equalsIgnoreCase(DIVISION)){
            	iSegundoOp = 190;
			}
            imageInByte = agregaElemntoImagen(imageInByte, vo2.getOperando1(),
            		                                iPrimerOp, iV2);
        	imageInByte = agregaElemntoImagen(imageInByte, vo2.getOperador(),
        			                                      -50, iV2);
        	imageInByte = agregaElemntoImagen(imageInByte, vo2.getOperando2(),
        			                               iSegundoOp, iV2);
        }
        
        return imageInByte;
    }

    /**
     * Metodo para agregar elementos a la imagen de captcha(operandos y operador)
     * @param stream una imagen previa.
     * @param ptTexto el texto para agregar a stream
     * @param pfPosX Coordenada X donde se colocara el texto dentro de stream
     * @param pfPosY Coordenada Y donde se colocara el texto dentro de stream
     * @return stream que contiene la imagen con el nuevo elemento ptText
     */
    private byte[] agregaElemntoImagen(byte[] stream,
    		                                  String ptTexto,
    		                                  float pfPosX,
    		                                  float pfPosY
    		                                 ){

        CaptchaParameters captcha = getCaptchaParameters(ptTexto);
        InputStream in = new ByteArrayInputStream(stream);
        BufferedImage image = null;
        try{
            image = ImageIO.read(in);
        }catch(IOException e){}  
        Graphics2D graphics2D = image.createGraphics();
        if (pfPosY < 0)
        	graphics2D.setColor(Color.WHITE);
        else
        	graphics2D.setColor(Color.YELLOW);
        AffineTransform oldTransform = graphics2D.getTransform();
        
        TextLayout tl = new TextLayout(ptTexto.trim(), 
        		  new Font(captcha.fontType, captcha.fontStyle, captcha.fontSize),
                             new FontRenderContext(null, false, false));
        AffineTransform textAt = new AffineTransform();
        textAt.translate(0, (float) tl.getBounds().getHeight());
        Shape textShape = tl.getOutline(textAt);
                
        AffineTransform transformer = new AffineTransform();
        transformer.setToIdentity();
        transformer.translate(captcha.width /2, captcha.height /2);
        transformer.rotate(Math.toRadians(captcha.rotate));
        //transformer.shear(captcha.shearX, captcha.shearY);
        AffineTransform toCenterAt = new AffineTransform();
        toCenterAt.concatenate(transformer);

        float fPosX = pfPosX;
        float fPosY = pfPosY;
        
        toCenterAt.translate(fPosX, fPosY);
        graphics2D.transform(toCenterAt);
        graphics2D.fill(textShape);
        graphics2D.draw(textShape);
        graphics2D.setTransform(oldTransform);
        graphics2D.dispose();
        
        /*
        // Las lineas siguientes deben ser eliminadas
        try {
        	ImageIO.write(image, "png", 
            new File("C:/temp/Captcha.png"));
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        // Las lineas anteriores deben ser eliminadas
        */
        
        
        tl = null;
        textShape = null;
        transformer = null;
        textAt = null;
        in = null;
        captcha = null;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
        	ImageIO.write(image, "PNG", outputStream);        
        	outputStream.flush();
        }catch(IOException IOEx){}

        image = null;
        return outputStream.toByteArray();
    }
    
    /**
     * Nombre de la subclase: CaptchaParameters
     * Version: 1.0.0
     * Descripcion de la version: Version inicial
     * Autor: Gerardo G. Burguete
     * Fecha: 20-08-2017
     */
    private class CaptchaParameters {
     
        private  String[] fontFamilies = { Font.DIALOG, Font.DIALOG_INPUT, 
        		  Font.MONOSPACED, Font.SANS_SERIF, Font.SERIF, "Arial" };
        
        private  int[] fontStyles = { Font.BOLD, Font.PLAIN, Font.ITALIC };
        public double rotate;
        //public double shearX;
        //public double shearY;
        public int fontSize;
        public String fontType;
        public int fontStyle;
        public int width;
        public int height;
        //public float positionX;
        @SuppressWarnings("unused")
		public float positionY;
        public GradientPaint gradient;
        public String text;
        @SuppressWarnings("unused")
		private String displayedText;
        public Color VERDE_IFT = new Color(45,92,38);

        /**
         * Metodo para para generar las medidas, rotacion y color del captcha.        
         * @param ptText Texto a rotar dentro del captcha
         */
        public CaptchaParameters(String ptText) {
            Random random = new Random(System.currentTimeMillis());

            this.text = ptText;
            this.displayedText = this.text;
            this.rotate = random.nextFloat() * 20 - 10;
            //this.shearX = random.nextFloat() - 0.5;
            //this.shearY = random.nextFloat() - 0.5;
            this.fontSize = 80;//random.nextInt(3) + (15 * 3);
            this.height = 300;
            this.width  = 600;
            
            this.positionY = random.nextFloat() * 7 - 5;
           
            switch (random.nextInt(6)) {
            	case 0:
            		this.gradient = new GradientPaint(0, 0, Color.GRAY, this.width,
            		                               this.height, Color.WHITE);
            		break;
            	case 1:
            		this.gradient = new GradientPaint(0, 0, Color.WHITE, this.width,
            		                               this.height, Color.GRAY);
            		break;
            	case 2:
            		this.gradient = new GradientPaint(this.width, 0, Color.BLUE, 0,
            		                               this.height, Color.WHITE);
            		break;
            	case 3:
            		this.gradient = new GradientPaint(0, this.height, Color.RED,
            		                             this.width, 0, Color.WHITE);
            		break;
            	case 4:
            		this.gradient = new GradientPaint(0, this.height, 
            		                Color.YELLOW, this.width, 0, Color.BLUE);
            		break;
            	case 5:
            		this.gradient = new GradientPaint(0, 0, VERDE_IFT, 
            		                   this.width, this.height, Color.WHITE);
            		break;
            }
            
            this.fontType = fontFamilies[random.nextInt(fontFamilies.length)];
            this.fontStyle = fontStyles[random.nextInt(fontStyles.length)];
        }
    }    
 
    /**
     * Geenera los parametros internos del captcha.
     * @param ptTexto
     * @return Clase Interna con parametros de generacion del captcha
     */
    private CaptchaParameters getCaptchaParameters(String ptTexto) {
        CaptchaParameters captchaParameters = new CaptchaParameters(ptTexto);
        return captchaParameters;
    }	
    
    public String[] splitOp(String op){
    	String[] a = new String[3];
    	StringTokenizer st = new StringTokenizer(op, " ");
    	int x = 0;
    	while (st.hasMoreElements()) {
    		a[x++] = (String)st.nextElement();
		}
    	return a;
    }
    
    public static void main(String...strings ){
    	ConvertidorImagenImpl i = new ConvertidorImagenImpl();
    	OperacionVO op = new OperacionVO();
    	
    	op.setOperacion("9 menos 7");
    	String[] a = i.splitOp(op.getOperacion());
    	
    	op.setOperando1(a[0]);
    	op.setOperador(a[1] + " ");
    	op.setOperando2(a[2]);
    	
    	OperacionVO op2 = new OperacionVO();
    	op2.setOperacion("8 menos 4");
    	a = i.splitOp(op2.getOperacion());
    	op2.setOperando1(a[0]);
    	op2.setOperador(a[1] + " ");
    	op2.setOperando2(a[2]);
    	
    	byte[] b = i.createCaptcha(op, op2);
    	
    }
}