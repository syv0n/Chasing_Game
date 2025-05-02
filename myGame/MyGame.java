package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.audio.*;

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

	private IAudioManager audioMgr;
	private Sound sunSound;

	private int counter = 0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject dol, sun, earth, moon, x, y, z, lava, dragon, person;
	private ObjShape dolS, sphS, pyrS, torS, linxS, linyS, linzS, ghostS, lavaS, dragonS;
	private TextureImage doltx, ghostT, lavatx, heightmap, dragontx, persontx;
	private Light light1;
	private AnimatedShape personS;

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
		dragonS = new ImportedModel("DragonFolk.obj");
		personS = new AnimatedShape("person.rkm", "person.rks");
		personS.loadAnimation("WAVE", "wave.rka");


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
		dragontx = new TextureImage("DragonFolk.png");
		persontx = new TextureImage("person.png");
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
		initialTranslation = (new Matrix4f()).translation(5, 1, 5);
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
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void loadSounds() {
		AudioResource resource = null;
		audioMgr = engine.getAudioManager();
		resource = audioMgr.createAudioResource("lava.wav", AudioResourceType.AUDIO_SAMPLE);
		sunSound = new Sound(resource, SoundType.SOUND_EFFECT, 100, true);
		sunSound.initialize(audioMgr);
		sunSound.setMaxDistance(10.0f);
		sunSound.setMinDistance(0.5f);
		sunSound.setRollOff(5.0f);
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// Initial sound settings
		sunSound.setLocation(sun.getWorldLocation());
		setEarPerimeters();
		sunSound.play();

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

	public void setEarPerimeters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(dol.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	public void update() {    

		sunSound.setLocation(sun.getWorldLocation());
		setEarPerimeters();
		
		elapsTime = System.currentTimeMillis() - lastFrameTime;
		lastFrameTime = System.currentTimeMillis();
		personS.updateAnimation();

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

		Vector3f loc = dol.getWorldLocation();
		float height = lava.getHeight(loc.x(), loc.z());
		dol.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

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
		cam.setLocation(loc.add(up.mul(1.3f)).add(fwd.mul(-4.0f)));
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

	private void switchAvatar(ObjShape shape, TextureImage texture) {
		// Get location and rotation
		Vector3f position = dol.getWorldLocation();
		Matrix4f rotation = new Matrix4f(dol.getWorldRotation());
		Matrix4f scale = new Matrix4f(dol.getLocalScale());

		// Remove game object
		engine.getSceneGraph().removeGameObject(dol);

		// Create new game object
		dol = new GameObject(GameObject.root(), shape, texture);
		dol.setLocalLocation(position);
		dol.setLocalRotation(rotation);
		dol.setLocalScale(scale);

		// notifying the networking system
		if (protClient != null) {
			protClient.sendMoveMessage(dol.getWorldLocation());
		}

		positionCameraBehind();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_1: {
				switchAvatar(demonS, demontx);
				break;
			}
			case KeyEvent.VK_2: {
				switchAvatar(dragonS, dragontx);
				break;
			}
			case KeyEvent.VK_3: {
				switchAvatar(dolS, doltx);
			}
			case KeyEvent.VK_Z: {
				personS.playAnimation("WAVE", 0.5f, AnimatedShape.EndType.LOOP, 0);
				break;
			}
			case KeyEvent.VK_X: {
				personS.stopAnimation();
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
