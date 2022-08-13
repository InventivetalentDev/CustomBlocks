

package org.inventivetalent.customblocks;

import org.inventivetalent.customblocks.data.Position;

import java.awt.image.BufferedImage;

public class ImageConverter {

    public static BufferedImage[][][] convertImage(BufferedImage original) {
        BufferedImage[][][] images = new BufferedImage[2][2][2];
        for (Position position : Position.values()) {
            images[position.getX()][position.getY()][position.getZ()] = position.drawImage(original);
        }
        return images;
    }

}
