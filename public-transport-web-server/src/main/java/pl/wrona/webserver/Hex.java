package pl.wrona.webserver;


import lombok.experimental.UtilityClass;

@UtilityClass
public class Hex {

    public static final int RADIX_36 = 36;

    public String toHex(Integer sequence) {
        // Converts an Integer to a zero-padded 4-character base-36 string
        return String.format("%4s", Integer.toString(sequence, RADIX_36)).replace(' ', '0');
    }

    public Integer fromHex(String hex) {
        // Reverts the toHex operation
        if (hex == null) {
            return null;
        }
        return Integer.parseInt(hex, RADIX_36);
    }

    public String toHex5(Integer sequence) {
        // Converts an Integer to a zero-padded 5-character base-36 string
        return String.format("%5s", Integer.toString(sequence, RADIX_36)).replace(' ', '0');
    }

    public String toHex3(Integer sequence) {
        // Converts an Integer to a zero-padded 3-character base-36 string
        return String.format("%3s", Integer.toString(sequence, RADIX_36)).replace(' ', '0');
    }

}
