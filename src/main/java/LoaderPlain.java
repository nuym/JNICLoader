public class LoaderPlain {
    public static void registerNativesForClass(int index, LoaderHelper clazz) {
    }

    static {
        System.loadLibrary("%LIB_NAME%");
    }
}
