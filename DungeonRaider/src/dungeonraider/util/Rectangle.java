package dungeonraider.util;

public class Rectangle {
	private int x;
	private int y;
	private int width;
	private int height;
	private int[] pixels;

	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void generateGraphics(int color) {
		pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixels[x + y * width] = color;
			}
		}
	}

	/**
	 * @return the pixels
	 */
	public int[] getPixels() {
		if (pixels != null) {
			return pixels;
		} else {
			System.out.println("Error,attempted to get pixels without generating pixels");
			return null;
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * This method will check to see if the new x and y being parsed in is within
	 * the boundaries of the hitbox.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(int x, int y) {
		if (this.x <= x && this.x + width >= x && this.y <= y && this.y + height >= y) {
			return true;
		}
		return false;
	}

}
