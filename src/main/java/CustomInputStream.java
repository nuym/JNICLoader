import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class CustomInputStream {

    private ArrayList<Long> positions;
    private int[] headers;
    private boolean append;

    int write(DataOutputStream dataOutputStream, boolean hasHeader) throws IOException {
        int length = 0;

        for (Long position : positions) {
            length += writeVariableLengthLong(dataOutputStream, position);
        }

        length += writeVariableLengthLong(dataOutputStream, 0L);

        if (hasHeader) {
            for (int i = 0; i < headers.length; i++) {
                length += 4;
                dataOutputStream.writeInt(headers[i]);
            }
        }

        return length;
    }

    public int compareTo(CustomInputStream other) {
        if (positions != null && other.positions != null) {
            Long firstPos = positions.get(0);
            Long otherFirstPos = other.positions.get(0);
            int positionComparison = firstPos.compareTo(otherFirstPos);
            if (positionComparison != 0) {
                return positionComparison;
            }
        }
        return Integer.compare(headers.length, other.headers.length);
    }int writeVariableLengthLong(OutputStream outputStream, long value) throws IOException {
        int length = 0;
        while ((value & -128L) != 0L) {
            outputStream.write((byte) (128L | value & 127L));
            length++;
            value >>>= 7;
        }
        length++;
        outputStream.write((byte) (value));
        return length;
    }
}
