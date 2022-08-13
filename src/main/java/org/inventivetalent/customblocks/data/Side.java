

package org.inventivetalent.customblocks.data;

import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
public enum Side {
    UP(8, 0),
    DOWN(16, 0),
    RIGHT(0, 8),
    FRONT(8, 8),
    LEFT(16, 8),
    BACK(24, 8);

    private final int srcX, srcY, srcWidth, srcHeight;

    Side(int srcX, int srcY, int srcWidth, int srcHeight) {
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcWidth = srcWidth;
        this.srcHeight = srcHeight;
    }

    Side(int srcX, int srcY) {
        this(srcX, srcY, 8, 8);
    }

    public Side opposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case RIGHT:
                return LEFT;
            case FRONT:
                return BACK;
            case LEFT:
                return RIGHT;
            case BACK:
                return FRONT;
        }
        return this;
    }

    /**
     * Gets the sub-image for this section from the original skin texture
     *
     * @param image skin texture
     * @return sub image
     */
    public BufferedImage subImage(BufferedImage image) {
        return image.getSubimage(this.srcX, this.srcY, this.srcWidth, this.srcHeight);
    }
}
