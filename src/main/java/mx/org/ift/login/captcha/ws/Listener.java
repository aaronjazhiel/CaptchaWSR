package mx.org.ift.login.captcha.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import mx.org.ift.login.captcha.service.AudioService;
import mx.org.ift.login.captcha.service.ImageService;
import mx.org.ift.login.captcha.service.impl.AudioServiceImpl;
import mx.org.ift.login.captcha.service.impl.ConvertidorImagenImpl;
import mx.org.ift.login.captcha.service.impl.ImageServiceImpl;
import mx.org.ift.login.captcha.vo.OperacionVO;


@Path("/captcha")
public class Listener {
	
	@GET
	@Path("/speech/{Message}")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	public String translate(@PathParam("Message")String message){
		AudioService as = new AudioServiceImpl();
		String speech = as.translate2(message);
		
		return speech;
	}
	
	@POST
	@Path("/content")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public String content(Integer it){
		ImageService is = new ImageServiceImpl();
		AudioService as = new AudioServiceImpl();
		
		OperacionVO op1 = is.operacion();
		String legend = "Escribe el resultado de " + op1.getOperacion();
		OperacionVO op2 = null;
		if(it > 1){
			op2 = is.operacion();
			legend += " y " + op2.getOperacion();
		}
		
		String speech = as.translate2(legend);
		
		String json = is.map2JSON(op1, op2, speech, it);
		System.out.println(json);
		return json;
	}
	
	@GET
	@Path("/operacion/{op1}/{op2}")
	@Produces("image/png")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response operacion(@PathParam("op1")String op1,
			                  @PathParam("op2")String op2){
		ImageService is = new ImageServiceImpl();
		
		ConvertidorImagenImpl i = new ConvertidorImagenImpl();
    	OperacionVO op = new OperacionVO();
    	
    	op.setOperacion(op1);
    	String[] a = i.splitOp(op.getOperacion());
    	
    	op.setOperando1(a[0]);
    	op.setOperador(a[1] + " ");
    	op.setOperando2(a[2]);
    	
    	OperacionVO op3 = new OperacionVO();
    	
    	if(op2 != null && op2.length() > 5){
	    	op3.setOperacion(op2);
	    	a = i.splitOp(op3.getOperacion());
	    	op3.setOperando1(a[0]);
	    	op3.setOperador(a[1] + " ");
	    	op3.setOperando2(a[2]);
    	}
    	final byte[] img = i.createCaptcha(op, op3);
    	
		/*
		StringTokenizer st = new StringTokenizer(" ");
		if(op2 != null && op2.length() > 5){
			op1 = op1 + "|" + op2;
		}
		
		final byte[] img = is.text2image(op1);*/
		
		ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		builder.header("Content-Disposition",
				"attachment; filename=image.png");
		
		builder.status(Status.OK).type("image/png").entity(new StreamingOutput(){
		    @Override
		    public void write(OutputStream output) throws IOException, WebApplicationException {
		        output.write(img);
		        output.flush();
		    }
		});
		return builder.build();
	}
	
}