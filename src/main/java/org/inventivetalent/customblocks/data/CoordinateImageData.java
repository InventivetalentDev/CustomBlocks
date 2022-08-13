

package org.inventivetalent.customblocks.data;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true,
        doNotUseGetters = true)
@ToString(callSuper = true,
        doNotUseGetters = true)
public class CoordinateImageData extends ImageData {

    protected int x;
    protected int y;
    protected int z;
    protected Position position;

    public CoordinateImageData(int x, int y, int z) {
        this(x, y, z, Position.get(x, y, z));
    }

    public CoordinateImageData(int x, int y, int z, String image, String value, String signature) {
        this(x, y, z);
        this.image = image;
        this.value = value;
        this.signature = signature;
    }

    public static CoordinateImageData from(ImageData data, int x, int y, int z) {
        return new CoordinateImageData(x, y, z, data.image, data.value, data.signature);
    }

}
