import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class LoaderHelper {


    public static void copyFile(String sourcePath, String destinationPath, String tempPath, String tempFileName) throws IOException {
        String fileName = sourcePath;
        if (tempPath != null && !"".equals(tempPath)) {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(sourcePath), 1048576);
            String tempFilePath = tempFileName;
            Inflater inflater = new Inflater();
            InflaterInputStream inflaterInput = new InflaterInputStream(input, inflater, 1048576);

            OutputStream output;
            OutputStream tempOutput = OpenOutputStream.open(tempPath, destinationPath, tempFilePath);

            copy(inflaterInput, tempOutput);

            inflater.end();
            inflaterInput.close();
            tempOutput.close();
        }
    }


    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }


    private static long readVariableLengthLong(InputStream inputStream) throws IOException {
        long value;
        if ((value = (long) inputStream.read()) < 0L) {
            throw new EOFException();
        } else if ((value = (long) ((byte) ((int) value))) >= 0L) {
            return value;
        } else {
            // 实现同原代码

            value &= 127L;

            for (int i = 7; i < 64; i += 7) {
                long byt;
                if ((byt = (long) inputStream.read()) < 0L) {
                    throw new EOFException();
                }

                byt = (long) ((byte) ((int) byt));
                value |= (byt & 127L) << i;
                if (byt >= 0L) {
                    return value;
                }
            }

            return value;
        }
    }

}