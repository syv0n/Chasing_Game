package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;
import tage.networking.IGameConnection.ProtocolType;

//test
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;

public class MyGame extends VariableFrameRateGame {
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private int counter = 0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject dol, sun, earth, moon, x, y, z, lava, demon;
	private ObjShape dolS, sphS, pyrS, torS, linxS, linyS, linzS, ghostS, lavaS, demonS;
	private TextureImage doltx, ghostT, lavatx, heightmap, demontx;
	private Light light1;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	//skybox
	private int hellscape;

	private float amtt = 0.0f;

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args) {
		MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes() {
		dolS = new ImportedModel("dolphinHighPoly.obj");
		demonS = new ImportedModel("demon2.obj");
		ghostS = new Sphere();
		sphS = new Sphere();
		linxS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(3f, 0f, 0f));
		linyS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 3f, 0f));
		linzS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 3f));

		lavaS = new TerrainPlane(1000);
	}

	@Override
	public void loadTextures() {
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		ghostT = new TextureImage("redDolphin.jpg");
		lavatx = new TextureImage("10001.png");
		heightmap = new TextureImage("testheightmap.png");
		demontx = new TextureImage("demon2.png");
	}

	@Override
	public void loadSkyBoxes() {
		hellscape = engine.getSceneGraph().loadCubeMap("dungeonWalls");
		engine.getSceneGraph().setActiveSkyBoxTexture(hellscape);
		engine.getSceneGraph().setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects() {
		Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0, 0, 0);
		initialScale = (new Matrix4f()).scaling(3.0f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);

		sun = new GameObject(GameObject.root(), sphS);
		initialTranslation = (new Matrix4f()).translation(1, 0, 1);
		initialScale = (new Matrix4f()).scaling(0.5f);
		sun.setLocalTranslation((initialTranslation));
		sun.setLocalScale(initialScale);

		//add X, Y, -Z axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(y.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(z.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		//build terrain
		lava = new GameObject(GameObject.root(), lavaS, lavatx);
		initialTranslation = new Matrix4f().translation(0f,0f,0f);
		lava.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(20.0f, 1.0f, 20.0f);
		lava.setLocalScale(initialScale);
		lava.setHeightMap(heightmap);
		
		lava.getRenderStates().setTiling(1);
		lava.getRenderStates().setTileFactor(10);

		// demon chaser
		demon = new GameObject(GameObject.root(), demonS, demontx);
		initialScale = new Matrix4f().scaling(1f,1f,1f);
		initialTranslation = new Matrix4f().translation(0f,0f,0f);
		demon.setLocalScale(initialScale);
		demon.setLocalTranslation(initialTranslation);
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// ------------- positioning the camera -------------
		//(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0, 0, 5));
		positionCameraBehind();
		im = engine.getInputManager();

		fwdAction FwdAction = new fwdAction(this);
		backAction BackAction = new backAction(this);
		leftAction LeftAction = new leftAction(this);
		rightAction RightAction = new rightAction(this);
		upAction UpAction = new upAction(this);
		downAction DownAction = new downAction(this);

		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.W, FwdAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.S, BackAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.A, LeftAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.D, RightAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.UP, UpAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(
				net.java.games.input.Component.Identifier.Key.DOWN, DownAction,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		setupNetworking();
	}

	@Override
	public void update() {    
		elapsTime = System.currentTimeMillis() - lastFrameTime;
		lastFrameTime = System.currentTimeMillis();

		// build and set HUD
		int elapsTimeSec = Math.round((float) elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;
		String dispStr2 = "Keyboard hits = " + counterStr;
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		Vector3f hud2Color = new Vector3f(0, 0, 1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, 500, 15);
		im.update((float) elapsTime);
		positionCameraBehind();
		processNetworking((float)elapsTime);
	}

	private void positionCameraBehind() {
		Vector3f loc, fwd, up, right;
		Camera cam;
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		loc = dol.getWorldLocation();
		fwd = dol.getWorldForwardVector();
		up = dol.getWorldUpVector();
		right = dol.getWorldRightVector();
		cam.setU(right);
		cam.setV(up);
		cam.setN(fwd);
		cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-2.5f)));
	}

	// CAMERA POV of AVATAR/PLAYER
	private void povCamera() {

	}

	// CAMERA positioned to look behind player
	private void lookBackCamera() {

	}

	// CAMERA follows skybox
	private void overViewCamera() {

	}

	public GameObject getDol() {
		return dol;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W: {
				Vector3f oldPosition = dol.getWorldLocation();
				Vector4f fwdDirection = new Vector4f(0f, 0f, 1f, 1f);
				fwdDirection.mul(dol.getWorldRotation());
				fwdDirection.mul(0.05f);
				Vector3f newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
				dol.setLocalLocation(newPosition);
				protClient.sendMoveMessage(dol.getWorldLocation());
				break;
			}
			case KeyEvent.VK_D: {
				Matrix4f oldRotation = new Matrix4f(dol.getWorldRotation());
				Vector4f oldUp = new Vector4f(0f, 1f, 0f, 1f).mul(oldRotation);
				Matrix4f rotAroundAvatarUp = new Matrix4f().rotation(-.01f, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
				Matrix4f newRotation = oldRotation;
				newRotation.mul(rotAroundAvatarUp);
				dol.setLocalRotation(newRotation);
				break;
			}
		}
		super.keyPressed(e);
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public GhostManager getGhostManager() {
		return gm;
	}

	public Engine getEngine() {
		return engine;
	}

	private void setupNetworking() {
		isClientConnected = false;
		try {
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (protClient == null) {
			System.out.println("missing protocol host");
		} else {    // Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}

	protected void processNetworking(float elapsTime) {    // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() {
		return dol.getWorldLocation();
	}

	public void setIsConnected(boolean value) {
		this.isClientConnected = value;
	}

	private class SendCloseConnectionPacketAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event evt) {
			if (protClient != null && isClientConnected == true) {
				protClient.sendByeMessage();
			}
		}
	}
}