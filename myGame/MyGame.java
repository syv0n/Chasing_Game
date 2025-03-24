package myGame;

import tage.*;
import tage.shapes.*;
//test
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

	private GameObject dol, sun, earth, moon;
	private ObjShape dolS, sphS, pyrS, torS;
	private TextureImage doltx;
	private Light light1;
	private float amtt = 0.0f;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	dolS = new ImportedModel("dolphinHighPoly.obj");
		sphS = new Sphere();
		pyrS = new Cube();
		torS = new Torus();
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("Dolphin_HighPolyUV.png");
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		sun = new GameObject(GameObject.root(), sphS);
		initialTranslation = new Matrix4f().translation(0,0,0);
		initialScale = new Matrix4f().scaling(0.5f);
		sun.setLocalTranslation((initialTranslation));
		sun.setLocalScale(initialScale);

		earth = new GameObject(GameObject.root(), pyrS);
		initialTranslation = new Matrix4f().translation(-1,0,0);
		earth.setLocalTranslation(initialTranslation);
		earth.setParent(sun);
		earth.propagateTranslation(true);
		earth.propagateRotation(false);

		moon = new GameObject(GameObject.root(), torS);
		initialTranslation = new Matrix4f().translation(0,1,0);
		moon.setLocalTranslation(initialTranslation);
		moon.setParent(earth);
		moon.propagateTranslation(true);
		moon.propagateRotation(false);
		moon.getRenderStates().setTiling(1);


		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		dol.setParent(earth);
		dol.propagateTranslation(true);
		dol.propagateRotation(true);
		dol.applyParentRotationToPosition(true);
		initialScale = new Matrix4f().scaling(0.1f);
		dol.setLocalScale(initialScale);
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

		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,5));
	}

	@Override
	public void update()
	{	// rotate dolphin if not paused
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		/*if (!paused) elapsTime += (currFrameTime - lastFrameTime) / 1000.0;
		dol.setLocalRotation((new Matrix4f()).rotation((float)elapsTime, 0, 1, 0));
		*/
		amtt += 0.01f;
		Matrix4f currentTranslation = earth.getLocalTranslation();
		currentTranslation.translation((float)Math.sin(amtt)*2.0f, 0.0f, (float)Math.cos(amtt)*2.0f);
		earth.setLocalTranslation(currentTranslation);

		// build and set HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "Keyboard hits = " + counterStr;
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(0,0,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);
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