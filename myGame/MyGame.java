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

// physics
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;


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

	private GameObject lava, dragon, person, plane, Box, sun;
	private ObjShape ghostS, lavaS, dragonS, npcS, planeS, BoxS, sphS;
	private TextureImage ghostT, lavatx, heightmap, dragontx, persontx, npctx, groundtx;
	private Light light1;
	private AnimatedShape personS;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// physics engine
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, caps2P, planeP;
	private boolean running = false;
	private float vals[] = new float[16];

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

		//player characters
		dragonS = new ImportedModel("DragonFolk.obj");
		personS = new AnimatedShape("person.rkm", "person.rks");
		personS.loadAnimation("WAVE", "wave.rka");

		// terrain + plane
		lavaS = new TerrainPlane(1000);
		planeS = new Plane();
		BoxS = new Cube();
		sphS = new Sphere();
	}

	@Override
	public void loadTextures() {

		// terrain
		lavatx = new TextureImage("10001.png");
		heightmap = new TextureImage("something.png");

		// plane 
		groundtx = new TextureImage("ground.jpg");

		// player characters
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

		person = new GameObject(GameObject.root(), personS, persontx);
		initialTranslation = new Matrix4f().translation(0, 1, 0);
		initialScale = new Matrix4f().scaling(0.2f);
		person.setLocalTranslation(initialTranslation);
		person.setLocalScale(initialScale);

		//build terrain
		lava = new GameObject(GameObject.root(), lavaS, lavatx);
		initialTranslation = new Matrix4f().translation(0f,-0.1f,0f);
		lava.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(20.0f, 1.0f, 20.0f);
		lava.setLocalScale(initialScale);
		lava.setHeightMap(heightmap);
		
		lava.getRenderStates().setTiling(1);
		lava.getRenderStates().setTileFactor(10);
		lava.getRenderStates().hasDepthTesting(true);

		// build plane
		plane = new GameObject(GameObject.root(), planeS, groundtx);
		initialTranslation = new Matrix4f().translation(0,0,0);
		initialScale = new Matrix4f().scaling(20.0f, 1.0f, 20.0f);
		plane.setLocalTranslation(initialTranslation);
		plane.setLocalScale(initialScale);
		plane.getRenderStates().hasDepthTesting(true);
		plane.getRenderStates().setColor(new Vector3f(1,1,1));

		Box = new GameObject(GameObject.root(), BoxS);
		initialTranslation = (new Matrix4f()).translation(7, 1, 7);
		initialScale = (new Matrix4f()).scaling(1.0f);
		Box.setLocalTranslation(initialTranslation);
		Box.setLocalScale(initialScale);

		sun = new GameObject(GameObject.root(), sphS);
		initialTranslation = (new Matrix4f()).translation(5, 1, 5);
		initialScale = (new Matrix4f()).scaling(0.5f);
		sun.setLocalTranslation(initialTranslation);
		sun.setLocalScale(initialScale);
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

		/*
		sunSound = new Sound(resource, SoundType.SOUND_EFFECT, 100, true);
		sunSound.initialize(audioMgr);
		sunSound.setMaxDistance(10.0f);
		sunSound.setMinDistance(0.5f);
		sunSound.setRollOff(5.0f);
		*/
	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// Initial sound settings
		//sunSound.setLocation(sun.getWorldLocation());
		//setEarPerimeters();
		//sunSound.play();

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

		// initialize physics system
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = engine.getSceneGraph().getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		float up[] = {0, 1, 0};
		float radius = 0.6f;
		float height = 0.35f;
		double[] tempTransform;

		Matrix4f translation = new Matrix4f(person.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		caps1P = (engine.getSceneGraph()).addPhysicsCapsule(0.0f, tempTransform, radius, height);
		caps1P.setBounciness(0.8f);
		person.setPhysicsObject(caps1P);

		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		planeP.setBounciness(1.0f);
		plane.setPhysicsObject(planeP);

		engine.enableGraphicsWorldRender();
		engine.enablePhysicsWorldRender();

	}

	public void setEarPerimeters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(person.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	public void update() {    

		//sunSound.setLocation(sun.getWorldLocation());
		//setEarPerimeters();
		
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

		terra();

		processNetworking((float)elapsTime);

		// MAKE PHYSICS OBJECT ATTACH TO GRAPHICS OBJECT
		if (person.getPhysicsObject() != null) {
			Matrix4f personTransform = person.getLocalTranslation();
			double[] physicsTransform = toDoubleArray(personTransform.get(vals));
			person.getPhysicsObject().setTransform(physicsTransform);
		}
		checkForCollisions();
	}

	public boolean isBlocked(Vector3f pos) {
		float height = lava.getHeight(pos.x(), pos.z());

		if (height > 1.0f) {
			return true;
		}
		return false;
	}

	private void terra() {
		Vector3f loc = person.getWorldLocation();
		Vector3f Ball = sun.getWorldLocation();
		float height1 = lava.getHeight(loc.x(), loc.z());
		float height2 = lava.getHeight(Ball.x(), Ball.z());
		person.setLocalLocation(new Vector3f(loc.x(), (height1 + 0.75f), loc.z()));
		if (!inGoal(sun))
			sun.setLocalLocation(new Vector3f(Ball.x(), height2 + .75f, Ball.z()));
	}

	private boolean inGoal(GameObject obj) {
		Vector3f Bloc = Box.getWorldLocation();
		Vector3f Oloc = obj.getWorldLocation();

		float tolerance = 1.0f;

		return Math.abs(Oloc.x() - Bloc.x()) < tolerance &&
				Math.abs(Oloc.z() - Bloc.z()) < tolerance;
	}

	private void positionCameraBehind() {
		Vector3f loc, fwd, up, right;
		Camera cam;
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		loc = person.getWorldLocation();
		fwd = person.getWorldForwardVector();
		up = person.getWorldUpVector();
		right = person.getWorldRightVector();
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

	public GameObject getPerson() {
		return person;
	}

	private void switchAvatar(ObjShape shape, TextureImage texture) {
		// Get location and rotation
		Vector3f position = person.getWorldLocation();
		Matrix4f rotation = new Matrix4f(person.getWorldRotation());
		Matrix4f scale = new Matrix4f(person.getLocalScale());

		// Remove game object
		engine.getSceneGraph().removeGameObject(person);

		// Create new game object
		person = new GameObject(GameObject.root(), shape, texture);
		person.setLocalLocation(position);
		person.setLocalRotation(rotation);
		person.setLocalScale(scale);

		// notifying the networking system
		if (protClient != null) {
			protClient.sendMoveMessage(person.getWorldLocation());
		}

		positionCameraBehind();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_1: {
				switchAvatar(personS, persontx);
				break;
			}
			case KeyEvent.VK_2: {
				switchAvatar(dragonS, dragontx);
				break;
			}
			case KeyEvent.VK_Z: {
				personS.playAnimation("WAVE", 0.5f, AnimatedShape.EndType.LOOP, 0);
				break;
			}
			case KeyEvent.VK_X: {
				personS.stopAnimation();
				break;
			}
			case KeyEvent.VK_SPACE: {
				System.out.println("starting physics");
				running = true;
				break;
			}
			case KeyEvent.VK_B: {
				Vector3f perPos = person.getWorldLocation();
				Vector3f ballPos = sun.getWorldLocation();

				if (!inGoal(sun)) {
					// Check distance
					if (perPos.distance(ballPos) < 2.5f) {
						Vector4f forward = new Vector4f(0f, 0f, 1f, 0f);
						forward.mul(person.getWorldRotation());

						//Add some kick
						Vector3f kickDir = new Vector3f(forward.x, forward.y, forward.z).normalize().mul(2f);

						//Move the ball
						Vector3f newBallPos = ballPos.add(kickDir);
						sun.setLocalLocation(newBallPos);

						System.out.println("Ball Kicked!");
						break;
					}
				}
			}
		}
		super.keyPressed(e);
	}

	// utility functions for physics

	private float[] toFloatArray(double[] arr) {
		if (arr == null) {
			return null;
		} 
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (float) arr[i];
		}
		return ret;
	}

	private double[] toDoubleArray(float[] arr) {
		if (arr == null) {
			return null;
		}
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}

	private void checkForCollisions() {
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i = 0; i < manifoldCount; i++) {
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++) {
				contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f) {
					//collision object
					System.out.println("hit between " + obj1 + "and " + obj2);
					break;
				}
			}
		}
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() {
		return ghostS;
	}

	public TextureImage getGhostTexture() {
		return ghostT;
	}

	public ObjShape getNPCshape() {
		return npcS;
	}

	public TextureImage getNPCtexture() {
		return npctx;
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
		return person.getWorldLocation();
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
