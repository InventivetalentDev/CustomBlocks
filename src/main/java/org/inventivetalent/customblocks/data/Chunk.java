

package org.inventivetalent.customblocks.data;

import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
public enum Chunk {
    TOP_LEFT(0, 0),
    TOP_RIGHT(4, 0),
    BOTTOM_LEFT(0, 4),
    BOTTOM_RIGHT(4, 4),
    ;

    private final int x, y;
    private final int width, height;

    Chunk(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    Chunk(int x, int y) {
        this(x, y, 4, 4);
    }

    /**
     * Get this chunk from a sub-image of the original skin (converted with {@link Side#subImage(BufferedImage)})
     *
     * @param image side texture
     * @return sub image
     */
    public BufferedImage subImage(BufferedImage image) {
        return image.getSubimage(this.x, this.y, this.width, this.height);
    }

}
