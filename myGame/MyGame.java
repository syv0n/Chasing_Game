package myGame;

import tage.*;
import tage.shapes.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;

	private boolean paused=false;
	private int counter=0;
	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject dol, lava, ground;
	private ObjShape dolS, lavaS, groundS;
	private TextureImage doltx, lavaTx, groundTx;
	private Light light1;
	private float amtt = 0.0f;

	//skybox 
	private int dungeonWalls;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		lavaS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures()
	{	
		lavaTx = new TextureImage("10001.png");
		groundTx = new TextureImage();
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialScale;
		// build terrain
		lava = new GameObject(GameObject.root(), lavaS, lavaTx);
		initialTranslation = new Matrix4f().tranlsation(0f,0f,0f);
		lava.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(20.0f, 1.0f, 20.0f);
		lava.setLocalScale(initialScale);
		
		lava.setHeightMap(heightMap);
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

	}

	@Override
	public void loadSkyBoxes() {
		dungeonWalls = engine.getSceneGraph().loadCubeMap("dungeonWalls");
		engine.getSceneGraph().setActiveSkyBoxTexture(dungeonWalls);
		engine.getSceneGraph().setSkyBoxEnabled(true);
		
	}

	@Override
	public void update()
	{	

	}

	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_C:
				counter++;
				break;
			case KeyEvent.VK_1:
				paused = !paused;
				break;
			case KeyEvent.VK_2:
				dol.getRenderStates().setWireframe(true);
				break;
			case KeyEvent.VK_3:
				dol.getRenderStates().setWireframe(false);
				break;
			case KeyEvent.VK_4:
				(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
		}
		super.keyPressed(e);
	}
}