import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JNICLoader {
    public static void registerNativesForClass(int index, Class clazz) {
    }


    static {
        String osname = System.getProperty("os.name").toLowerCase();
        String osarch = System.getProperty("os.arch").toLowerCase();
        String arch = "raw"+osarch;
        String name = "raw"+osname;
        switch (osarch) {
            case "x86_64":
            case "amd64": {
                arch = "x64";
                break;
            }
            case "aarch64": {
                arch = "arm64";
                break;
            }
            case "arm": {
                arch = "arm32";
                break;
            }
            case "x86": {
                arch = "x86";
                break;
            }
        }
        if (osname.contains("nix") || osname.contains("nux") || osname.contains("aix")) {
            name = "linux.so";
        } else if (osname.contains("win")) {
            name = "windows.dll";
        } else if (osname.contains("mac")) {
            name = "macos.dylib";
        }
        String data = String.format("/%s/data.dat", JNICLoader.class.getPackage().getName().replace(".", "/"));
        File lib;
        File dat;
        try {
            File temp = new File(System.getProperty("java.io.tmpdir"));
            if (!temp.exists()) {
                temp.mkdirs();
            }
            lib = File.createTempFile("lib", null);
            dat = File.createTempFile("dat", null);
            lib.deleteOnExit();
            dat.deleteOnExit();
            if (!lib.exists())
                throw new IOException();
            if (!dat.exists())
                throw new IOException();
        }
        catch (IOException a7) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }
        byte[] bytes = new byte[2048];
        try {
            InputStream inputStream = JNICLoader.class.getResourceAsStream(data);
            if (inputStream == null) {
                throw new UnsatisfiedLinkError(String.format("Failed to open dat file: %s", data));
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(dat);){
                int n;
                while ((n=inputStream.read())!=-1) {
                    fileOutputStream.write(n);
                }
                inputStream.close();
                fileOutputStream.close();
            }
        }
        catch (IOException exception) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
        }
        try {
            DataTool.extract(dat.getAbsolutePath(),System.getProperty("java.io.tmpdir"),arch+"-"+name,lib.getName());
        }
        catch (Exception e) {
            System.out.println(new StringBuilder().insert(0, "Failed load library:").append(lib.getAbsolutePath()).toString());
            throw new RuntimeException(e);
        }
        try {
            System.load(lib.getAbsolutePath());
        }
        catch (UnsatisfiedLinkError e) {
            System.out.println(new StringBuilder().insert(0, "Failed load library:").append(lib.getAbsolutePath()).toString());
            e.printStackTrace();
        }
    }
}
