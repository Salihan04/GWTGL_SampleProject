package com.han.gwtgl.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.gwtgl.array.Float32Array;
import com.googlecode.gwtgl.binding.WebGLBuffer;
import com.googlecode.gwtgl.binding.WebGLProgram;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLShader;
import com.googlecode.gwtgl.binding.WebGLUniformLocation;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GWTGL_SampleProject implements EntryPoint {
	
	private WebGLRenderingContext glContext;
	private WebGLProgram shaderProgram;
	private int vertexPositionAttribute;
	private WebGLBuffer vertexBuffer;
	private int vertexBufferItemSize;
	private int vertexBufferNumItems;
	
	private WebGLShader getShader(int type, String source) {
		WebGLShader shader = glContext.createShader(type);
		
		glContext.shaderSource(shader, source);
		glContext.compileShader(shader);
		
		if(!glContext.getShaderParameterb(shader, WebGLRenderingContext.COMPILE_STATUS))
			throw new RuntimeException(glContext.getShaderInfoLog(shader));
		
		return shader;
	}
	
	private void initShaders() {
		WebGLShader fragmentShader = getShader(WebGLRenderingContext.FRAGMENT_SHADER, 
				Shaders.INSTANCE.fragmentShader().getText());
		WebGLShader vertexShader = getShader(WebGLRenderingContext.VERTEX_SHADER, 
				Shaders.INSTANCE.vertexShader().getText());
		
		shaderProgram = glContext.createProgram();
		glContext.attachShader(shaderProgram, vertexShader);
		glContext.attachShader(shaderProgram, fragmentShader);
		glContext.linkProgram(shaderProgram);
		
		if(!glContext.getProgramParameterb(shaderProgram, WebGLRenderingContext.LINK_STATUS))
			throw new RuntimeException(glContext.getProgramInfoLog(shaderProgram));
		
		glContext.useProgram(shaderProgram);
		
		vertexPositionAttribute = glContext.getAttribLocation(shaderProgram, "aVertexPosition");
		glContext.enableVertexAttribArray(vertexPositionAttribute);
	}
	
	private void initBuffers() {
		vertexBuffer = glContext.createBuffer();
		glContext.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexBuffer);
		
		float[] vertices = {
				0.0f, 1.0f, 0.0f, 
				-1.0f, -1.0f, 0.0f,
				1.0f, -1.0f, 0.0f
		};
		glContext.bufferData(WebGLRenderingContext.ARRAY_BUFFER, Float32Array.create(vertices), 
				WebGLRenderingContext.STATIC_DRAW);
		vertexBufferItemSize = 3;
		vertexBufferNumItems = 3;
	}
	
	private float[] createPerspectiveMatrix(int fieldOfViewVertical, float aspectRatio, 
			float minClearance, float maxClearance) {
		
		float top = (float) (minClearance * Math.tan(fieldOfViewVertical * Math.PI / 360));
		float bottom = -top;
		float left = bottom * aspectRatio;
		float right = top * aspectRatio;
		
		float X = 2 * minClearance / (right - left);
		float Y = 2 * minClearance / (top - bottom);
		float A = (right + left) / (right - left);
		float B = (top + bottom) / (top - bottom);
		float C = -(maxClearance + minClearance) / (maxClearance - minClearance);
		float D = -2 * maxClearance * minClearance / (maxClearance - minClearance);
		
		return new float[]{
				X, 0.0f, A, 0.0f,
				0.0f, Y, B, 0.0f,
				0.0f, 0.0f, C, -1.0f,
				0.0f, 0.0f, D, 0.0f
		};
	}
	
	private void drawScene() {
		glContext.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
		
		float[] perspectiveMatrix = createPerspectiveMatrix(45, 1, 0.1f, 1000);
		
		WebGLUniformLocation uPerspectiveMatrix = glContext.getUniformLocation(shaderProgram, 
				"uPerspectiveMatrix");
		glContext.uniformMatrix4fv(uPerspectiveMatrix, false, perspectiveMatrix);
		glContext.vertexAttribPointer(vertexPositionAttribute, vertexBufferItemSize, 
				glContext.FLOAT, false, 0, 0);
		glContext.drawArrays(glContext.TRIANGLES, 0, vertexBufferNumItems);
	}
	
	private void start() {
		initShaders();
		glContext.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glContext.clearDepth(1.0f);
		glContext.enable(WebGLRenderingContext.DEPTH_TEST);
		glContext.depthFunc(WebGLRenderingContext.LEQUAL);
		initBuffers();
		
		drawScene();
	}
	
	@Override
	public void onModuleLoad() {
		final Canvas webGLCanvas = Canvas.createIfSupported();
		webGLCanvas.setCoordinateSpaceWidth(500);
		webGLCanvas.setCoordinateSpaceHeight(500);
		
		glContext = (WebGLRenderingContext) webGLCanvas.getContext("webgl");
		if(glContext == null) {
			Window.alert("Sorry, your browser does not support WebGL");
		}
		glContext.viewport(0, 0, 500, 500);
		
		RootPanel.get("gwtGL").add(webGLCanvas);
		start();
	}
}
