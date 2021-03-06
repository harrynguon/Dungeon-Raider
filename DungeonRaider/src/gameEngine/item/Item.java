package gameEngine.item;



import gameEngine.engine.Renderer;
import gameEngine.sprite.Sprite;
import gameEngine.util.Box;
import gameEngine.util.Position;

public abstract class Item {

	protected Position position;
	protected Boolean pickedUp;
	private Sprite sprite;
	private Box boundingBox;

	public Item(int x, int y, Sprite sprite) {
		this.position = new Position(x, y);
		pickedUp = false;
		this.sprite = sprite;
		boundingBox = new Box(x, y, sprite.getWidth()*6, sprite.getHeight()*6);
	}

	public Boolean getPickedUp() {
		return pickedUp;
	}

	public void setPickedUp(Boolean pickedUp) {
		this.pickedUp = pickedUp;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Position getItemCenter() {
		Position center = new Position(position.getX() + sprite.getHeight()*Renderer.ZOOM/2, position.getY() + sprite.getHeight()*Renderer.ZOOM/2);
		return center;
	}

	public Box getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(Box boundingBox) {
		this.boundingBox = boundingBox;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

}
