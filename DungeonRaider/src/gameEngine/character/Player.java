package gameEngine.character;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import gameEngine.controller.KeyController;
import gameEngine.engine.Engine;
import gameEngine.engine.GameObject;
import gameEngine.engine.Renderer;
import gameEngine.item.Consumable;
import gameEngine.item.Item;
import gameEngine.item.Weapon;
import gameEngine.map.Map;
import gameEngine.sprite.AnimatedSprite;
import gameEngine.sprite.Sprite;
import gameEngine.sprite.SpriteSheet;
import gameEngine.util.Box;
import gameEngine.util.Position;
import gameEngine.util.Rectangle;
import library3.Movement;
import library4.Saveable;

/**
 * Need to implement equiping of weapon then test if attacking works because
 * right now null pointer exeception when trying to find weapon range
 *
 * @author Gabriel Tennent
 *
 */

public class Player implements Character, GameObject, Saveable {

	private int hp;
	private int hpMax;
	private int gold;
	private int x;
	private int y;

	/**
	 * Item and bounding box variables
	 */
	private Weapon primaryWeapon;
	private Inventory inventory;
	private Sprite spriteImage;
	private Rectangle playerBoundBox;
	private int zoom;
	private Queue<Integer> damageQueue;

	private Sprite sprite;
	private AnimatedSprite animatedSprite = null;
	private int speed = 3;
	// 0 == right, 1 == left, 2 == up, 3 == down. This is based off the
	// Player.png
	private int direction = 0;
	// for the player animated sprites
	boolean didMove;
	int newDirection;
	boolean couldntRun;

	private Rectangle playerAttack;
	private Boolean attacking;
	private int attackTimer;
	public static final int ATTACKTIME = 8;

	private Sprite[] swords = new Sprite[4];

	/**
	 * Player visual radius
	 */
	private int radius;

	public Player(Position center, int stamina, int zoom, int hp, int hpMax,
			int radius) {
		this.damageQueue = new PriorityQueue<>();
		loadSprites();
		this.radius = radius;
		this.zoom = zoom;
		this.zoom = 3;
		this.x = center.getX() - (spriteImage.getWidth() / 2 * zoom);
		this.y = center.getY() - (spriteImage.getHeight() / 2 * zoom);
		this.hp = hp;
		this.hpMax = hpMax;
		this.inventory = new Inventory(20);
		this.primaryWeapon = new Weapon("Start", 0, 0, 1, 300, 10, spriteImage);
		this.sprite = spriteImage;
		if (sprite != null && sprite instanceof AnimatedSprite) {
			this.animatedSprite = (AnimatedSprite) spriteImage;
		}
		this.playerBoundBox = new Rectangle(x + 11, y + 62,
				(int) (animatedSprite.getWidth() * zoom * 0.6 - 2),
				(int) (animatedSprite.getHeight() * zoom * 0.2));
		this.playerBoundBox.generateGraphics(Color.green.getRGB());

		this.didMove = false;
		this.newDirection = this.direction;
		this.couldntRun = false;
		this.attackTimer = 0;

		//loads the sword images and initiates attacking state
		loadImages();
		this.attacking = false;

		playerAttack = new Rectangle(
				playerBoundBox.getX() + playerBoundBox.getWidth() / 2,
				y + 32, primaryWeapon.getRange(),
				playerBoundBox.getHeight() * 2);
		this.playerAttack.generateGraphics(Color.green.getRGB());

	}

	/**
	 * loading
	 *
	 * @param hp
	 * @param hpMax
	 * @param gold
	 * @param x
	 * @param y
	 * @param primaryWeapon
	 * @param inventory
	 * @param playerBoundBox
	 * @param zoom
	 * @param direction
	 * @param radius
	 */
	public Player(int hp, int hpMax, int gold, int x, int y,
			Weapon primaryWeapon, Inventory inventory, Rectangle playerBoundBox,
			int zoom, int direction, int radius) {
		super();
		this.hp = hp;
		this.hpMax = hpMax;
		this.gold = gold;
		this.x = x;
		this.y = y;
		this.primaryWeapon = primaryWeapon;
		this.inventory = inventory;
		this.playerBoundBox = playerBoundBox;
		this.zoom = zoom;
		this.direction = direction;
		this.radius = radius;

	}

	private void loadImages() {
		//Loads all of swords image into sword image array
		this.swords[0] = new Sprite(Engine.loadImage("resources/images/SwordEast.png"));
		this.swords[1] = new Sprite(Engine.loadImage("resources/images/SwordWest.png"));
		this.swords[2] = new Sprite(Engine.loadImage("resources/images/SwordNorth.png"));
		this.swords[3] = new Sprite(Engine.loadImage("resources/images/SwordSouth.png"));
		this.primaryWeapon.setRange(swords[0].getWidth()*3);
	}

	private void updateDirection() {
		if (animatedSprite != null) {
			animatedSprite.setAnimationRange(direction * 8, direction * 8 + 7);
		}
	}

	@Override
	public int heavyAttack() {
		return primaryWeapon.getDamage();
	}

	@Override
	public void walkLeft() {
		this.x -= Movement.WALK_SPEED;
		// this.playerBoundBox.setX(this.playerBoundBox.getX() - SPEED);
		this.playerBoundBox.setX(Movement.walkLeft(this.playerBoundBox.getX(),
				Movement.WALK_SPEED));
	}

	@Override
	public void walkRight() {
		this.x += Movement.WALK_SPEED;
		// this.playerBoundBox.setX(this.playerBoundBox.getX() + SPEED);
		this.playerBoundBox.setX(Movement.walkRight(this.playerBoundBox.getX(),
				Movement.WALK_SPEED));
	}

	@Override
	public void walkUp() {
		this.y -= Movement.WALK_SPEED;
		// this.playerBoundBox.setY(this.playerBoundBox.getY() - SPEED);
		this.playerBoundBox.setY(Movement.walkUp(this.playerBoundBox.getY(),
				Movement.WALK_SPEED));
	}

	@Override
	public void walkDown() {
		this.y += Movement.WALK_SPEED;
		// this.playerBoundBox.setY(this.playerBoundBox.getY() + SPEED);
		this.playerBoundBox.setY(Movement.walkDown(this.playerBoundBox.getY(),
				Movement.WALK_SPEED));
	}

	public void runLeft() {
		this.x -= Movement.SPRINT_SPEED;
		// this.playerBoundBox.setX(this.playerBoundBox.getX() - SPRINT);
		this.playerBoundBox
		.setX(Movement.sprintLeft(this.playerBoundBox.getX()));
	}

	public void runRight() {
		this.x += Movement.SPRINT_SPEED;
		// this.playerBoundBox.setX(this.playerBoundBox.getX() + SPRINT);
		this.playerBoundBox
		.setX(Movement.sprintRight(this.playerBoundBox.getX()));
	}

	public void runUp() {
		this.y -= Movement.SPRINT_SPEED;
		// this.playerBoundBox.setY(this.playerBoundBox.getY() - SPRINT);
		this.playerBoundBox.setY(Movement.sprintUp(this.playerBoundBox.getY()));
	}

	public void runDown() {
		this.y += Movement.SPRINT_SPEED;
		// this.playerBoundBox.setY(this.playerBoundBox.getY() + SPRINT);
		this.playerBoundBox
		.setY(Movement.sprintDown(this.playerBoundBox.getY()));
	}

	public void interact() {

	}

	/**
	 * Renders the players current position
	 */
	@Override
	public void render(Renderer renderer, int xZoom, int yZoom) {
		// introducing the animated sprite here. initially rendering a static
		// sprite.
		//renderer.renderRectangle(playerBoundBox, 1, 1);






	//	renderer.renderRectangle(playerAttack, 1, 1);
		if (animatedSprite != null)
			renderer.renderSprite(animatedSprite,
					x + animatedSprite.getWidth() / 2,
					y + animatedSprite.getHeight() / 2, xZoom, yZoom);
		else if (sprite != null)
			renderer.renderSprite(sprite, x + animatedSprite.getWidth() / 2,
					y + animatedSprite.getHeight() / 2, xZoom, yZoom);
		else
			renderer.renderArray(spriteImage.getPixels(),
					spriteImage.getWidth(), spriteImage.getWidth(), x, y, zoom,
					zoom);



		renderAttack(renderer);
	}

	public void renderAttack(Renderer renderer) {
		if(attacking)
		if(newDirection == 0) {
			//right sword
			renderer.renderSprite(swords[0], (int) (playerBoundBox.getX() + playerBoundBox.getWidth()) - 5, y + 30, 2, 2);
		} else if (newDirection == 1) {
			//left sword
			renderer.renderSprite(swords[1], (int) (playerBoundBox.getX() - (swords[1].getWidth()*2) + 5), y + 30, 2, 2);
		} else if (newDirection == 2) {
		//up sword
		renderer.renderSprite(swords[2], playerBoundBox.getX()-6, y - (swords[1].getHeight()*2) + 35, 2, 2);
		} else if (newDirection == 3) {
		//down sword
		renderer.renderSprite(swords[3], playerBoundBox.getX()-6, y + 80, 2, 2);
		}


	}

	/**
	 * Updates the player dependant on the key and mouse controllers in the
	 * engine and what booleans have been activated via the user pushing keys /
	 * the mouse.
	 */
	@Override
	public void update(Engine engine) {
		// for the player animated sprites
		didMove = false;
		newDirection = direction;
		couldntRun = false;

		// Attempts to pick up an item
		pickUp(engine);
		// Checks if the player has taken damage
		checkDamage();
		// Checks if the player is using an item
		useItem(engine);
		// Checks if player is running
		tryRun(engine);
		// Checks if player is walking
		if (!engine.getKeyBinds().isRun())
			tryWalk(engine);
		// Checks if player teleporting
		checkTeleportation(engine);
		// Checks if player is attacking
		if(checkAttack())
		attack(engine);
		// Updates playing animations accordingly
		updateAnimations(engine);

	}

	@Override
	public void attack(Engine engine) {
		KeyController keyBinds = engine.getKeyBinds();
		if (keyBinds.isAttack() && !attacking) {
			this.attacking = true;
			// player is looking horizontally
			if (this.direction == 0 || this.direction == 1) {
				// Constructing horizontal bounding box
				playerAttack = new Rectangle(
						playerBoundBox.getX() + playerBoundBox.getWidth() / 2,
						y + 38, primaryWeapon.getRange(),
						playerBoundBox.getHeight() * 2);
				if (this.direction == 0) { // Player is looking right
					attackMonster(engine, playerAttack);
				} else if (this.direction == 1) { // Player is looking left
					playerAttack.setX( // adjusts weapon attack to left
							playerAttack.getX() - playerAttack.getWidth());
					attackMonster(engine, playerAttack);
				}
			} else { // Player is looking vertically
				// Constructing verticle bounding box
				playerAttack = new Rectangle(playerBoundBox.getX(),
						(y + 64) - primaryWeapon.getRange(),
						playerBoundBox.getWidth(), primaryWeapon.getRange());
				if (this.direction == 2) { // Player is looking up
					attackMonster(engine, playerAttack);// Calls attack
				} else if (this.direction == 3) { // Player is looking down
					playerAttack.setY(y + 60); // adjusts weapon attack to up
					attackMonster(engine, playerAttack); // Calls attack

				}
			}
		}
	}

	public boolean checkAttack() {
		if(attackTimer < ATTACKTIME) {
			attackTimer++;
			return false;
		} else {
			this.attacking = false;
			attackTimer = 0;
			return true;
		}
	}

	/**
	 * Checks if the player is stepping on a teleporter
	 *
	 * @param engine
	 */
	public void checkTeleportation(Engine engine) {
		Map currentMap = engine.getCurrentMap();
		Box box = this.playerBoundBox;
		if (checkTeleporter(currentMap, box)) {
			if (engine.getCurrentMapNumber() == 3) {
				// so it doesn't go to a non-existing map
				return;
			}
			engine.setCurrentMap(
					engine.getMapList().get(engine.getCurrentMapNumber()));
			this.x = 200;
			this.y = 200;
			this.playerBoundBox.setX(this.x + 10);
			this.playerBoundBox.setY(this.y + 63);
			// this.playerBoundBox.generateGraphics(Color.blue.getRGB());
		}
	}

	// Charnon comment
	public void updateAnimations(Engine engine) {
		/**
		 * Updates camera
		 */
		// only update the direction if the player moves in a different
		// direction
		if (newDirection != direction) {
			direction = newDirection;
			updateDirection();
		}

		if (!didMove) {
			// makes sure that the sprite doesnt stop mid movement
			animatedSprite.reset();
		}

		// update the counter
		if (didMove) {
			animatedSprite.update(engine);
		}

		this.updateCamera(engine.getRenderer().getCamera());
	}

	public void tryRun(Engine engine) {
		Box curBox = this.playerBoundBox;
		KeyController keyBinds = engine.getKeyBinds();
		Map currentMap = engine.getCurrentMap();
		if (keyBinds.isRun()) {
			if (keyBinds.isUp()) {
				attacking = false;
				Box up = new Box(curBox.getX(), curBox.getY(),
						curBox.getWidth(), curBox.getHeight());
				// up.setY(up.getY() - SPRINT);
				up.setY(Movement.sprintUp(up.getY()));
				if (checkBoundry(currentMap, up)) {
					newDirection = 2;
					didMove = true;
					runUp();
				} else {
					tryWalk(engine);
				}
			}
			if (keyBinds.isDown()) {
				attacking = false;
				Box down = new Box(curBox.getX(), curBox.getY(),
						curBox.getWidth(), curBox.getHeight());
				// down.setY(down.getY() + SPRINT);
				down.setY(Movement.sprintDown(down.getY()));
				if (checkBoundry(currentMap, down)) {
					newDirection = 3;
					didMove = true;
					runDown();
				} else {
					tryWalk(engine);
				}
			}
			if (keyBinds.isLeft()) {
				attacking = false;
				Box left = new Box(curBox.getX(), curBox.getY(),
						curBox.getWidth(), curBox.getHeight());
				// left.setX(left.getX() - SPRINT);
				left.setX(Movement.sprintLeft(left.getX()));
				if (checkBoundry(currentMap, left)) {
					newDirection = 1;
					didMove = true;
					runLeft();
				} else {
					tryWalk(engine);
				}
			}
			if (keyBinds.isRight()) {
				attacking = false;
				Box right = new Box(curBox.getX(), curBox.getY(),
						curBox.getWidth(), curBox.getHeight());
				// right.setX(right.getX() + SPRINT);
				right.setX(Movement.sprintRight(right.getX()));
				if (checkBoundry(currentMap, right)) {
					newDirection = 0;
					didMove = true;
					runRight();
				} else {
					tryWalk(engine);
				}
			}
		}
	}

	public void tryWalk(Engine engine) {
		Box curBox = this.playerBoundBox;
		KeyController keyBinds = engine.getKeyBinds();
		Map currentMap = engine.getCurrentMap();
		/**
		 * Player walking connection with key controller: Also checks for player
		 * connection with walls
		 */
		if (keyBinds.isUp()) {
			attacking = false;
			Box up = new Box(curBox.getX(), curBox.getY() - Movement.WALK_SPEED,
					curBox.getWidth(), curBox.getHeight());
			if (checkBoundry(currentMap, up)) {
				newDirection = 2;
				didMove = true;
				walkUp();
			}
		}
		if (keyBinds.isDown()) {
			attacking = false;
			Box down = new Box(curBox.getX(),
					curBox.getY() + Movement.WALK_SPEED, curBox.getWidth(),
					curBox.getHeight());
			if (checkBoundry(currentMap, down)) {
				newDirection = 3;
				didMove = true;
				walkDown();
			}
		}
		if (keyBinds.isLeft()) {
			attacking = false;
			Box left = new Box(curBox.getX() - Movement.WALK_SPEED,
					curBox.getY(), curBox.getWidth(), curBox.getHeight());
			if (checkBoundry(currentMap, left)) {
				newDirection = 1;
				didMove = true;
				walkLeft();
			}
		}
		if (keyBinds.isRight()) {
			attacking = false;
			Box right = new Box(curBox.getX() + Movement.WALK_SPEED,
					curBox.getY(), curBox.getWidth(), curBox.getHeight());
			if (checkBoundry(currentMap, right)) {
				newDirection = 0;
				didMove = true;
				walkRight();
			}
		}

	}

	public void attackMonster(Engine engine, Box playerAttack) {
		// Ierates through all monsters
		List<GameObject> monsters = engine.getCurrentMap().getMonsters();
		Iterator<GameObject> iterator = monsters.iterator();
		while (iterator.hasNext()) {
			Monster mon = (Monster) iterator.next();
			// Checks if there is a monster in range of the player Attack
			if (playerAttack.contains(mon.getBoundingBox())) {
				// Adjusts monsters health accordingly
				mon.setHealth(mon.getHealth() - heavyAttack());
				// Checks is the monster is dead
				checkForMonsterDeath(mon, monsters, iterator);
			}
		}
	}

	/**
	 * Attempts to use a players item to heal the player
	 */
	public void useItem(Engine engine) {
		KeyController keyBinds = engine.getKeyBinds();
		// If player is trying to use an item
		if (keyBinds.isUseItem()) {
			// Checks if the players inventory is not empty
			if (!this.inventory.getInventory().isEmpty()) {
				// Returns the item as a health potion
				Consumable healthPot = (Consumable) this.inventory
						.returnFirstItem();
				// Heals the player = to that of the potions strength
				this.heal(healthPot.getHealingStrength());
			}
			// Prevents two potions from being used with one press
			keyBinds.setUseItem(false);
		}
	}

	/**
	 * Checks if the player has taken damage
	 */
	public void checkDamage() {
		/**
		 * Updates the players hpbar if they have taken damage
		 */
		if (!damageQueue.isEmpty()) {
			int damage = damageQueue.poll();
			this.hp += damage;
		}
	}

	/**
	 * Attemps to pick up item from the ground if within character range
	 *
	 * @param engine
	 *            contains all of the values required for picking up
	 */
	public void pickUp(Engine engine) {
		KeyController keyBinds = engine.getKeyBinds();
		// Checks if player is attempting to pick up item
		if (keyBinds.isPickUp()) {
			// Returns all of the items on the map
			List<Item> itemsOnMap = engine.getCurrentMap().getItems();
			for (Item item : itemsOnMap) {
				Box itemPos = item.getBoundingBox();
				// Checks if the player is standing within pickup range
				if (this.playerBoundBox.contains(itemPos)) {
					// Adds item to players inventory
					this.inventory.add(item);
					// Sets the item pickup value to true
					item.setPickedUp(true);
					// Removes the item from the map
					itemsOnMap.remove(item);
					break;
				}
			}
		}
	}

	public void checkForMonsterDeath(Monster monster, List<GameObject> monsters,
			Iterator<GameObject> iter) {
		if (monster.getHealth() <= 0) {
			this.gold = this.gold + 1;
			System.out.println("monster died");
			iter.remove();
		}

	}

	/**
	 * Updates the camera's position to center the player
	 *
	 * @param camera
	 */
	private void updateCamera(Box camera) {
		camera.setX(x - (camera.getWidth() / 2)
				+ (this.spriteImage.getWidth() * zoom / 2));
		camera.setY(y - (camera.getHeight() / 2)
				+ (this.spriteImage.getHeight() * zoom / 2));
	}

	/**
	 * Getters and setters for the player class
	 */

	/**
	 * @return the hp
	 */
	public int getHp() {
		return hp;
	}

	/**
	 * @return the hpMax
	 */
	public int getHpMax() {
		return hpMax;
	}

	public boolean isDead() {
		if (hp <= 0)
			return true;
		else
			return false;
	}

	/**
	 * Damages player with positive numbers, heals player with negative numbers
	 *
	 * @param i
	 */
	public void damage(int i) {
		this.damageQueue.offer(-i);
	}

	/**
	 * Heals player with negative numbers
	 *
	 * @param i
	 */
	public void heal(int i) {
		this.damageQueue.offer(i);
	}

	public Queue<Integer> getDamageQueue() {
		return damageQueue;
	}

	public int getGoldTotal() {
		return gold;
	}

	public void spendGold(int amountSpent) {
		gold = gold - amountSpent;
	}

	public void collectgold(int amountRecieved) {
		gold = gold + amountRecieved;
		;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Sprite getSpriteImage() {
		return spriteImage;
	}

	public int getSpeed() {
		return Movement.WALK_SPEED;
	}

	public int getZoom() {
		return zoom;
	}

	public Box getPlayerBoundBox() {
		return this.playerBoundBox;
	}

	public void setPlayerBoundBox(Box playerBoundBox) {
		this.playerBoundBox = (Rectangle) playerBoundBox;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	@Override
	public String save() {
		String s = "Player	{\n";
		s += "int	hp	" + hp + "\n";
		s += "int	hpMax	" + hpMax + "\n";
		s += "int	gold	" + gold + "\n";
		s += "int	x	" + x + "\n";
		s += "int	y	" + y + "\n";
		s += "primaryWeapon	primaryWeapon	" + primaryWeapon.save() + "\n";
		s += "inventory	inventory	" + inventory.save() + "\n";
		s += "zoom	zoom	" + zoom + "\n";
		s += "damageQueue	damageQueue	" + damageQueue + "\n";
		s += "direction	direction	" + direction + "\n";
		s += "}	\n";

		return s;
	}

	@Override
	public void load(File file) {
		// TODO Auto-generated method stub

	}

	public Weapon getPrimaryWeapon() {
		return primaryWeapon;
	}

	private void loadSprites() {
		BufferedImage playerSheetImage = Engine
				.loadImage("resources/images/Player.png");
		SpriteSheet playerSheet = new SpriteSheet(playerSheetImage);
		playerSheet.loadSprites(20, 26);

		AnimatedSprite playerAnimations = new AnimatedSprite(playerSheet, 5);
		this.spriteImage = playerAnimations;
	}

	/**
	 * Checks if x and y is within the player radius
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean checkRadius(int x, int y) {
		int pX = this.playerBoundBox.getX() + this.playerBoundBox.getWidth() / 2
				+ 15;
		int pY = this.playerBoundBox.getY()
				+ this.playerBoundBox.getHeight() / 2;
		int dx = Math.abs(x - pX);
		int dy = Math.abs(y - pY);
		int pythagoris = (int) (Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
		if (pythagoris <= this.radius) {
			return true;
		}
		return false;
	}
}