package dungeonraider.engine;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import dungeonraider.character.Player;
import dungeonraider.controller.KeyController;
import dungeonraider.map.Map;
import dungeonraider.sprite.Sprite;
import dungeonraider.sprite.SpriteSheet;
import dungeonraider.util.Camera;
import dungeonraider.util.PatternInt;
import dungeonraider.util.Rectangle;

/**
 * The main engine class which implements runnable and also contains the main
 * method.
 *
 * @author Jono Yan
 *
 */
public class Engine extends JFrame implements Runnable, Observer {
	public static final PatternInt alpha = new PatternInt(0xFFFF00DC, -16777216);
	private static final long serialVersionUID = 1L;
	private Canvas canvas;
	private Toolkit tk;
	private Renderer renderer;
	private List<Player> players;
	public static int WIDTH = 1280;
	public static int HEIGHT = 720;

	/**
	 * Key listener - keeps track of cameras movement
	 */
	private KeyController keyBinds;

	private Player player;

	/** This will contain the list of maps from start to finish */
	private HashMap<Integer, Map> mapList = initialiseMaps();
	private Map currentMap = mapList.get(0);
	int x = 0;
	/** Test Objects */
	private BufferedImage testImage;
	private Rectangle testRect;
	private Sprite playerSprite;
	private SpriteSheet testSpriteSheet;
	private SpriteSheet dungeonTiles = new SpriteSheet(loadImage("resources/tiles/DungeonTileset1.png"));

	public Engine() {
		this.canvas = new Canvas();
		this.tk = this.getToolkit();
		/**
		 * initiating key listener
		 */
		this.keyBinds = new KeyController();
		/** Sets name of JFrame window */
		setTitle("Dungeon Raider");
		/** Close program on exit */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/** Set location of JFrame window and size */
		setBounds(0, 0, WIDTH, HEIGHT);
		/** Sets window to center */
		setLocationRelativeTo(null);
		/** Adds canvas to JFrame */
		add(canvas);
		/** Disable Resizeable */
		setResizable(false);
		/** Sets JFrame to visible */
		setVisible(true);
		/** Component listener to see if JFrame is resized */
		/** Creates 2 buffer renderer */
		canvas.createBufferStrategy(3);
		this.renderer = new Renderer(getWidth(), getHeight());

		/**
		 * Testing Objects
		 */
		this.testRect = new Rectangle(30, 90, 40, 40);
		this.testRect.generateGraphics(10, 356);
		testImage = loadImage("resources/tiles/grassTile.PNG");
		BufferedImage sheet = loadImage("resources/tiles/Tiles1.png");
		testSpriteSheet = new SpriteSheet(sheet);
		testSpriteSheet.loadSprites(16, 16);
		dungeonTiles.loadSprites(16, 16);
		this.playerSprite = dungeonTiles.getSprite(4, 6);
		System.out.println(this.playerSprite.toString());

		/**
		 * Initiating the players
		 */
		players = new ArrayList<Player>();
		this.player = new Player(100, 100, 100, playerSprite);
		this.addKeyListener(keyBinds);
		this.setFocusable(true);
	}

	/**
	 * This method will render everything onto the screen
	 */
	public void render() {
		BufferStrategy b = canvas.getBufferStrategy();
		Graphics g = b.getDrawGraphics();
		super.paint(g);
		// Renders the map first (bottom layer of the image)
		/** Set black first */
		renderer.clearArray();
		renderer.renderMap(currentMap);
		renderer.renderSprite(player.getSpriteImage(), player.getX(), player.getY(), 10, 10);

		/** Then render the Renderer */
		renderer.render(g);
		g.dispose();
		b.show();
	}

	/**
	 * Thread will execute this code and run the game.
	 */
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		/** 60 FPS */
		double nanoSecondConversion = 1000000000.0 / 60;
		double changeInSeconds = 0;
		while (true) {
			long now = System.nanoTime();
			changeInSeconds += (now - lastTime) / nanoSecondConversion;

			while (changeInSeconds >= 1) {
				update();
				changeInSeconds = 0;
			}
			render();
			lastTime = now;
		}
	}

	/**
	 * Updates Renderer if JFrame is resized
	 */
	public void updateFrame() {
		this.renderer.updateSize(WIDTH, HEIGHT);
	}

	public static void main(String[] args) {
		Engine game = new Engine();
		Thread thread = new Thread(game);
		thread.start();
	}

	/**
	 * This method gets called when the Observable class calls setChanged() &
	 * notifyObservers()
	 */
	@Override
	public void update(Observable o, Object arg) {
		update();
	}

	/**
	 * This method will update to the buffer. EG. char movement
	 *
	 * This method will run at a specified speed.
	 */
	public void update() {
		Camera camera = renderer.getCamera();
		if (keyBinds.isUp())
			renderer.getCamera().moveCamera(0, -player.getSpeed());
		if (keyBinds.isDown())
			renderer.getCamera().moveCamera(0, player.getSpeed());
		if (keyBinds.isLeft())
			renderer.getCamera().moveCamera(-player.getSpeed(), 0);
		if (keyBinds.isRight())
			renderer.getCamera().moveCamera(player.getSpeed(), 0);
		this.player.setX(camera.getCenter().getX());
		this.player.setY(camera.getCenter().getY());
	}

	/**
	 * Returns a buffered image from the path
	 *
	 * @param path
	 */
	public static BufferedImage loadImage(String path) {
		BufferedImage loadedImage;
		try {
			loadedImage = ImageIO.read(new FileInputStream(path));
			BufferedImage format = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			format.getGraphics().drawImage(loadedImage, 0, 0, null);
			return format;
		} catch (IOException e) {
			e.printStackTrace();
			loadedImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			loadedImage.getGraphics().setColor(Color.PINK);
			loadedImage.getGraphics().fillRect(0, 0, 30, 30);
			return loadedImage;
		}

	}

	/**
	 * This method is responsible for creating every map instance at the beginning,
	 * and storing it for later use until the player traverses through to each map.
	 *
	 * @return list of maps
	 */
	private HashMap<Integer, Map> initialiseMaps() {
		HashMap<Integer, Map> mapList = new HashMap<Integer, Map>();
		int count = 0;
		// Tutorial map
		Map tutMap = new Map();
		tutMap.initialiseMap("TutorialMap");
		mapList.put(count, tutMap);
		count++;
		return mapList;

	}

}
