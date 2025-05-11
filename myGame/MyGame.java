package myGame;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.audio.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.sql.Array;
import java.util.*;
import java.util.List;
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
	private Sound ballS1, ballS2, ballS3, GameMusic;

	private int counter = 0;
	private Vector3f currentPosition;
	private Matrix4f initialTranslation, initialRotation, initialScale;
	private double startTime, prevTime, elapsedTime, amt;

	private double lastFrameTime, currFrameTime, elapsTime;

	private GameObject lava, dragon, person, plane, box1, box2, box3 , ball1, ball2, ball3;
	private ObjShape ghostS, lavaS, dragonS, npcS, planeS, BoxS, sphS;
	private TextureImage ghostT, lavatx, heightmap, dragontx, persontx, npctx, groundtx, Box1, Box2, Box3, Ball1, Ball2, Ball3;
	private Light light1, ballLight1, ballLight2, ballLight3;
	private AnimatedShape personS;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// physics engine
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, sph1, sph2, sph3, planeP;
	private boolean running = false;
	private float vals[] = new float[16];

	//skybox
	private int hellscape;

	private float amtt = 0.0f;
	private int score = 0;
	private boolean ballScored = false;
	private boolean Goal1 = true;
	private boolean Goal2 = true;
	private boolean Goal3 = true;

	List<GameObject> physicsObjects = new ArrayList<>();

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

		ghostS = new Sphere();
		npcS = new Cube();
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

		ghostT = new TextureImage("redDolphin.jpg");
		npctx = new TextureImage("redDolphin.jpg");

		Box1 = new TextureImage("slime.jiggle.png");
		Box2 = new TextureImage("Chest.png");
		Box3 = new TextureImage("MagentaGoal.png");
		Ball1 = new TextureImage("Blue.png");
		Ball2 = new TextureImage("Brown.png");
		Ball3 = new TextureImage("Magenta.png");
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
		initialTranslation = new Matrix4f().translation(5, 1, -5);
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

		box1 = new GameObject(GameObject.root(), BoxS, Box1);
		initialTranslation = (new Matrix4f()).translation(7, 1, 7);
		initialScale = (new Matrix4f()).scaling(1.0f);
		box1.setLocalTranslation(initialTranslation);
		box1.setLocalScale(initialScale);

		box2 = new GameObject(GameObject.root(), BoxS, Box2);
		initialTranslation = (new Matrix4f()).translation(-7, 1, -7);
		initialScale = (new Matrix4f()).scaling(1.0f);
		box2.setLocalTranslation(initialTranslation);
		box2.setLocalScale(initialScale);

		box3 = new GameObject(GameObject.root(), BoxS, Box3);
		initialTranslation = (new Matrix4f()).translation(3, 1, -7);
		initialScale = (new Matrix4f()).scaling(1.0f);
		box3.setLocalTranslation(initialTranslation);
		box3.setLocalScale(initialScale);

		ball1 = new GameObject(GameObject.root(), sphS, Ball1);
		initialTranslation = (new Matrix4f()).translation(5, 2, 3);
		initialScale = (new Matrix4f()).scaling(0.5f);
		ball1.setLocalTranslation(initialTranslation);
		ball1.setLocalScale(initialScale);

		ball2 = new GameObject(GameObject.root(), sphS, Ball2);
		initialTranslation = (new Matrix4f()).translation(-5, 2, 3);
		initialScale = (new Matrix4f()).scaling(0.5f);
		ball2.setLocalTranslation(initialTranslation);
		ball2.setLocalScale(initialScale);

		ball3 = new GameObject(GameObject.root(), sphS, Ball3);
		initialTranslation = (new Matrix4f()).translation(-7, 2, 4);
		initialScale = (new Matrix4f()).scaling(0.5f);
		ball3.setLocalTranslation(initialTranslation);
		ball3.setLocalScale(initialScale);
	}

	@Override
	public void initializeLights() {
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);

		// set lights on balls
		ballLight1 = new Light();
		ballLight1.setDiffuse(1, 0, 0);
		ballLight1.setSpecular(1, 0, 0);
		ballLight1.setConstantAttenuation(1);
		ballLight1.setLinearAttenuation(0.2f);
		ballLight1.setQuadraticAttenuation(0.1f);
		engine.getSceneGraph().addLight(ballLight1);

		ballLight2 = new Light();
		ballLight2.setDiffuse(0, 1, 0);
		ballLight2.setSpecular(0, 1, 0);
		ballLight2.setConstantAttenuation(1);
		ballLight2.setLinearAttenuation(0.2f);
		ballLight2.setQuadraticAttenuation(0.1f);
		engine.getSceneGraph().addLight(ballLight2);

		ballLight3 = new Light();
		ballLight3.setDiffuse(0, 0, 1);
		ballLight3.setSpecular(0, 0, 1);
		ballLight3.setConstantAttenuation(1);
		ballLight3.setLinearAttenuation(0.2f);
		ballLight3.setQuadraticAttenuation(0.1f);
		engine.getSceneGraph().addLight(ballLight3);
	}

	private void updateLightPositions() {
		Vector3f pos1 = ball1.getWorldLocation();
		ballLight1.setLocation(new Vector3f(pos1.x(), pos1.y()+1f, pos1.z()));

		Vector3f pos2 = ball2.getWorldLocation();
		ballLight2.setLocation(new Vector3f(pos2.x(), pos2.y()+1f, pos2.z()));

		Vector3f pos3 = ball3.getWorldLocation();
		ballLight3.setLocation(new Vector3f(pos3.x(), pos3.y()+1f, pos3.z()));
	}

	@Override
	public void loadSounds() {
		AudioResource Goal1 = null;
		AudioResource Goal2 = null;

		audioMgr = engine.getAudioManager();
		Goal1 = audioMgr.createAudioResource("Victory!.wav", AudioResourceType.AUDIO_SAMPLE);
		Goal2 = audioMgr.createAudioResource("Game-Music.wav", AudioResourceType.AUDIO_STREAM);


		ballS1 = new Sound(Goal1, SoundType.SOUND_EFFECT, 100, true);
		ballS1.initialize(audioMgr);
		ballS1.setMaxDistance(10.0f);
		ballS1.setMinDistance(0.5f);
		ballS1.setRollOff(5.0f);

		ballS2 = new Sound(Goal1, SoundType.SOUND_EFFECT, 100, true);
		ballS2.initialize(audioMgr);
		ballS2.setMaxDistance(10.0f);
		ballS2.setMinDistance(0.5f);
		ballS2.setRollOff(5.0f);

		ballS3 = new Sound(Goal1, SoundType.SOUND_EFFECT, 100, true);
		ballS3.initialize(audioMgr);
		ballS3.setMaxDistance(10.0f);
		ballS3.setMinDistance(0.5f);
		ballS3.setRollOff(5.0f);

		GameMusic = new Sound(Goal2, SoundType.SOUND_MUSIC, 10, true);
		GameMusic.initialize(audioMgr);
		GameMusic.play();

	}

	@Override
	public void initializeGame() {
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900, 1000);

		// Initial sound settings
		ballS1.setLocation(box1.getWorldLocation());
		ballS2.setLocation(box2.getWorldLocation());
		ballS3.setLocation(box3.getWorldLocation());
		setEarPerimeters();

		// ------------- positioning the camera -------------
		//(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0, 0, 5));
		positionCameraBehind();
		im = engine.getInputManager();
		setupNetworking();

		fwdAction FwdAction = new fwdAction(this, protClient);
		backAction BackAction = new backAction(this, protClient);
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
		// LOGITECH F310
		InputController move = new InputController(this, "move", protClient);
		InputController turn = new InputController(this, "turn", protClient);
		InputController wave = new InputController(this, "wave", protClient);

		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, move, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RX, turn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Button._0 , wave, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// initialize physics system
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = engine.getSceneGraph().getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		float up[] = {0, 1f, 0};
		float radius = 0.6f;
		float height = 0.35f;
		double[] tempTransform;
		Matrix4f initialTranslation;

		Matrix4f translation = new Matrix4f(person.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		caps1P = (engine.getSceneGraph()).addPhysicsCapsule(0.0f, tempTransform, radius, height);
		caps1P.setBounciness(0.8f);
		person.setPhysicsObject(caps1P);

		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, -1.0f);
		planeP.setBounciness(0.3f);
		plane.setPhysicsObject(planeP);

		initialTranslation = new Matrix4f(ball1.getLocalTranslation());
		double[] physicsTransform = toDoubleArray(initialTranslation.get(vals));
		sph1 = (engine.getSceneGraph()).addPhysicsSphere(1.0f, physicsTransform, height);
		sph1.setBounciness(0.8f);
		ball1.setPhysicsObject(sph1);
		ball1.getPhysicsObject().setDamping(0.5f, 0.6f);

		initialTranslation = new Matrix4f(ball2.getLocalTranslation());
		tempTransform = toDoubleArray(initialTranslation.get(vals));
		sph2 = (engine.getSceneGraph()).addPhysicsSphere(1.0f, tempTransform, height);
		sph2.setBounciness(0.8f);
		ball2.setPhysicsObject(sph2);
		ball2.getPhysicsObject().setDamping(0.5f, 0.6f);

		initialTranslation = new Matrix4f(ball3.getLocalTranslation());
		tempTransform = toDoubleArray(initialTranslation.get(vals));
		sph3 = (engine.getSceneGraph()).addPhysicsSphere(1.0f, tempTransform, height);
		sph3.setBounciness(0.8f);
		ball3.setPhysicsObject(sph3);
		ball3.getPhysicsObject().setDamping(0.5f, 0.6f);

		//engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();

		physicsObjects.add(ball1);
		physicsObjects.add(ball2);
		physicsObjects.add(ball3);
	}

	public void setEarPerimeters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(person.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	public void update() {

		//sunSound.setLocation(sun.getWorldLocation());
		setEarPerimeters();

		elapsTime = System.currentTimeMillis() - lastFrameTime;
		lastFrameTime = System.currentTimeMillis();
		personS.updateAnimation();

		// build and set HUD
		String dispStr1 = "Score = " + score;
		Vector3f hud1Color = new Vector3f(1, 0, 0);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);

		im.update((float) elapsTime);
		positionCameraBehind();
		processNetworking((float) elapsTime);

		terra();


		// MAKE PHYSICS OBJECT ATTACH TO GRAPHICS OBJECT
		if (person.getPhysicsObject() != null) {
			Matrix4f personTransform = person.getLocalTranslation();
			double[] physicsTransform = toDoubleArray(personTransform.get(vals));
			person.getPhysicsObject().setTransform(physicsTransform);
		}

		if (running) {
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();

			physicsEngine.update((float) elapsTime);
			for (GameObject go : physicsObjects) {
				if (go.getPhysicsObject() != null) {
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					float x = mat.m30();
					float y = mat.m31();
					float z = mat.m32();

					// Terrain height at the ball's XZ position
					float desiredHeight = lava.getHeight(x, z) + 0.30f;
					float diff = desiredHeight - y;

					// Get vertical velocity component from velocity array
					float[] velocity = go.getPhysicsObject().getLinearVelocity();
					float vy = velocity[1];

					// Spring-damper force calculation
					float k = 50f;  // spring strength
					float d = 10f;  // damping

					float forceY = (k * diff) - (d * vy);

					// Apply force vertically at the center of the ball
					go.getPhysicsObject().applyForce(0f, forceY, 0f, 0f, 0f, 0f);

					// Update rendering transform
					mat2.set(3, 0, x);
					mat2.set(3, 1, y);
					mat2.set(3, 2, z);
					go.setLocalTranslation(mat2);

					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
				}
			}

		}
		checkForCollisions();
		if (inGoal(ball1, box1)) {
			ball1.getPhysicsObject().setLinearVelocity(new float[] {0f, 0f, 0f});
			ball1.getPhysicsObject().setAngularVelocity(new float[] {0f, 0f, 0f});

			physicsEngine.removeObject(ball1.getPhysicsObject().getUID());

			ball1.setLocalLocation(box1.getWorldLocation());
			if (Goal1){
				Goal1 = false;
				score++;
				ballS1.play();
			}
		}

		if (inGoal(ball2, box2)) {
			ball2.getPhysicsObject().setLinearVelocity(new float[] {0f, 0f, 0f});
			ball2.getPhysicsObject().setAngularVelocity(new float[] {0f, 0f, 0f});

			physicsEngine.removeObject(ball2.getPhysicsObject().getUID());

			ball2.setLocalLocation(box2.getWorldLocation());
			if (Goal2){
				Goal2 = false;
				score++;
				ballS2.play();
			}
		}

		if (inGoal(ball3, box3)) {
			ball3.getPhysicsObject().setLinearVelocity(new float[] {0f, 0f, 0f});
			ball3.getPhysicsObject().setAngularVelocity(new float[] {0f, 0f, 0f});

			physicsEngine.removeObject(ball3.getPhysicsObject().getUID());

			ball3.setLocalLocation(box3.getWorldLocation());
			if (Goal3) {
				Goal3 = false;
				score++;
				ballS3.play();
			}
		}

		updateLightPositions();
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
		float height1 = lava.getHeight(loc.x(), loc.z());
		person.setLocalLocation(new Vector3f(loc.x(), (height1 + 0.75f), loc.z()));
	}

	private boolean inGoal(GameObject ball, GameObject box) {
		Vector3f ballPos = ball.getWorldLocation();
		Vector3f boxPos = box.getWorldLocation();

		float tolerance = 1.0f; // Adjust based on object sizes

		return Math.abs(ballPos.x() - boxPos.x()) < tolerance &&
				Math.abs(ballPos.y() - boxPos.y()) < tolerance &&
				Math.abs(ballPos.z() - boxPos.z()) < tolerance;
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

	public AnimatedShape getPersonAnimatedShape() {
		return personS;
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

				Vector3f position = person.getWorldLocation();
				Matrix4f rotation = new Matrix4f(person.getWorldRotation());
				Matrix4f scale = new Matrix4f(person.getLocalScale());

				// Remove game object
				engine.getSceneGraph().removeGameObject(person);

				// Create new game object
				person = new GameObject(GameObject.root(), dragonS, dragontx);
				person.setLocalLocation(position);
				person.setLocalRotation(rotation);
				person.setLocalScale(scale);
				person.setPhysicsObject(caps1P);

				// notifying the networking system
				if (protClient != null) {
					protClient.sendMoveMessage(person.getWorldLocation());
				}

				positionCameraBehind();

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
		return person.getShape();
	}

	public TextureImage getGhostTexture() {
		return person.getTextureImage();
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
