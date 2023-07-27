import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class LoaderUnpack {
    public static void registerNativesForClass(int index, Class clazz) {
    }

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        String archVariant;

        if (osArch.equals("x86_64") || osArch.equals("amd64")) {
            archVariant = "x64";
        } else if (osArch.equals("aarch64")) {
            archVariant = "arm64";
        } else if (osArch.equals("arm")) {
            archVariant = "arm32";
        } else if (osArch.equals("x86")) {
            archVariant = "x86";
        } else {
            archVariant = "raw" + osArch;
        }

        if (osName.contains("win")) {
            archVariant = "windows.dll";
        } else if (osName.contains("mac")) {
            archVariant = "macos.dylib";
        } else if (!osName.contains("nix") && !osName.contains("nux") && !osName.contains("aix")) {
            archVariant = "raw" + osName;
        } else {
            archVariant = "linux.so";
        }

        String packageName = LoaderUnpack.class.getPackage().getName().replace(".", "/");
        String libFileName =  String.format("/%s/data.dat", LoaderUnpack.class.getPackage().getName().replace(".", "/").replace("JNICLoader/",""));
        String a = String.format("/%s/data.dat", LoaderUnpack.class.getPackage().getName().replace(".", "/"));

        File tempLibFile;
        File tempDataFile;
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            tempLibFile = File.createTempFile("lib", null);
            tempDataFile = File.createTempFile("dat", null);
            tempLibFile.deleteOnExit();
            tempDataFile.deleteOnExit();
            if (!tempLibFile.exists()) {
                throw new IOException();
            }
            if (!tempDataFile.exists()) {
                throw new IOException();
            }
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }

        byte[] buffer = new byte[2048];
        try (InputStream inputStream = LoaderUnpack.class.getResourceAsStream(libFileName);
             FileOutputStream outputStream = new FileOutputStream(tempDataFile)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", e.getMessage()));
        }
            try {
                //tmpLibPath, System.getProperty("java.io.tmpdir"), libName, dataFileName
                System.out.println(tempLibFile.getAbsolutePath());
                System.out.println((new StringBuilder()).insert(0, archVariant).append("-").append(a).toString());
                System.out.println(tempDataFile.getName());
                LoaderHelper.copyFile(tempLibFile.getAbsolutePath(), System.getProperty("java.io.tmpdir"),(new StringBuilder()).insert(0, archVariant).append("-").append(a).toString(),tempDataFile.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
            System.load(tempDataFile.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Failed to load library: " + tempDataFile.getAbsolutePath());
            e.printStackTrace();
        }

            /*
                  try {
         iiiiiIiIIi.goto(a.getAbsolutePath(), System.getProperty("java.io.tmpdir"), (new StringBuilder()).insert(0, var9).append("-").append(a).toString(), a.getName());
      } catch (Exception var15) {
         System.out.println((new StringBuilder()).insert(0, "Failed load library:").append(a).toString());
         throw new RuntimeException(var15);
      }

      a.deleteOnExit();
      String a = a.getAbsolutePath();

      try {
         System.load(a);
      } catch (UnsatisfiedLinkError var14) {
         System.out.println((new StringBuilder()).insert(0, "Failed load library:").append(a.getAbsolutePath()).toString());
         var14.printStackTrace();
      }
             */
    }
}
