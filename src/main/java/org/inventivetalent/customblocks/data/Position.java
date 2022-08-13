

package org.inventivetalent.customblocks.data;

import lombok.Getter;

import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
public enum Position {
    // down-front-left
    DFL(0, 0, 0, new Side[]{
            Side.DOWN,
            Side.FRONT,
            Side.LEFT
    }, new Chunk[]{
            Chunk.BOTTOM_LEFT,
            Chunk.BOTTOM_LEFT,
            Chunk.BOTTOM_RIGHT
    }),
    // down-front-right
    DFR(1, 0, 0, new Side[]{
            Side.DOWN,
            Side.FRONT,
            Side.RIGHT
    }, new Chunk[]{
            Chunk.BOTTOM_RIGHT,
            Chunk.BOTTOM_RIGHT,
            Chunk.BOTTOM_LEFT
    }),

    // down-back-left
    DBL(0, 0, 1, new Side[]{
            Side.DOWN,
            Side.BACK,
            Side.LEFT
    }, new Chunk[]{
            Chunk.TOP_LEFT,
            Chunk.BOTTOM_RIGHT,
            Chunk.BOTTOM_LEFT
    }),
    // down-back-right
    DBR(1, 0, 1, new Side[]{
            Side.DOWN,
            Side.BACK,
            Side.RIGHT
    }, new Chunk[]{
            Chunk.TOP_RIGHT,
            Chunk.BOTTOM_LEFT,
            Chunk.BOTTOM_RIGHT
    }),

    // up-front-left
    UFL(0, 1, 0, new Side[]{
            Side.UP,
            Side.FRONT,
            Side.LEFT
    }, new Chunk[]{
            Chunk.BOTTOM_LEFT,
            Chunk.TOP_LEFT,
            Chunk.TOP_RIGHT
    }),
    // up-front-right
    UFR(1, 1, 0, new Side[]{
            Side.UP,
            Side.FRONT,
            Side.RIGHT
    }, new Chunk[]{
            Chunk.BOTTOM_RIGHT,
            Chunk.TOP_RIGHT,
            Chunk.TOP_LEFT
    }),

    // up-back-left
    UBL(0, 1, 1, new Side[]{
            Side.UP,
            Side.BACK,
            Side.LEFT
    }, new Chunk[]{
            Chunk.TOP_LEFT,
            Chunk.TOP_RIGHT,
            Chunk.TOP_LEFT
    }),
    // up-back-right
    UBR(1, 1, 1, new Side[]{
            Side.UP,
            Side.BACK,
            Side.RIGHT
    }, new Chunk[]{
            Chunk.TOP_RIGHT,
            Chunk.TOP_LEFT,
            Chunk.TOP_RIGHT
    });

    private final int x, y, z;
    private final Side[] sides;
    private final Chunk[] chunks;

    Position(int x, int y, int z, Side[] sides, Chunk[] chunks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sides = sides;
        this.chunks = chunks;
    }

    /**
     * Splits the original image and draws a new texture
     *
     * @param original original image to split
     * @return new image
     */
    public BufferedImage drawImage(BufferedImage original) {
        BufferedImage newImage = new BufferedImage(64, 32, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = newImage.createGraphics();
        for (int i = 0; i < sides.length; i++) {
            BufferedImage sideSub = sides[i].subImage(original);
            BufferedImage chunkSub = chunks[i].subImage(sideSub);
            Image resized = chunkSub.getScaledInstance(8, 8, Image.SCALE_FAST);
            graphics.drawImage(resized, sides[i].getSrcX(), sides[i].getSrcY(), 8, 8, null);
            graphics.drawImage(resized, sides[i].opposite().getSrcX(), sides[i].opposite().getSrcY(), 8, 8, null);
        }
        graphics.dispose();
        return newImage;
    }

    public static Position get(int x, int y, int z) {
        for (Position position : values()) {
            if (position.x == x && position.y == y && position.z == z) {return position;}
        }
        return null;
    }
}
