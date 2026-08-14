package pl.wrona.webserver;


import lombok.experimental.UtilityClass;

@UtilityClass
public class Hex {

    public String toHex(Integer sequence) {
        // Converts an Integer to a zero-padded 4-character base-36 string
        return String.format("%4s", Integer.toString(sequence, 36)).replace(' ', '0');
    }

    public Integer fromHex(String hex) {
        // Reverts the toHex operation
        if (hex == null) {
            return null;
        }
        return Integer.parseInt(hex, 36);
    }

}
