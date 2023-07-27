import java.io.*;

public class OpenOutputStream {

    public static OutputStream open(final String tempPath, final String destinationPath, final String tempFileName) {
        File tempDirectory = new File(tempPath);
        tempDirectory.mkdirs();
        return new OutputStream() {
            private ByteArrayOutputStream byteArrayOutputStream;
            private OutputStream outputStream;
            private File file;
            private long size;
            private long remainingSize;
            private boolean append;
            final String name;
            {
                this.name = tempFileName;
                this.byteArrayOutputStream = new ByteArrayOutputStream();
                this.size = (long) tempFileName.length();
            }

            public void write(byte[] buffer, int offset, int length) throws IOException {
                // 实现同原代码
                byte[] tempBuffer = buffer;

                while (length > 0) {
                    if (this.outputStream != null && this.remainingSize > 1L) {
                        int size = (int) Math.min((long) length , this.remainingSize - 1L);
                        if (!this.file.getName().equals(tempFileName) || tempFileName == null) {
                            this.outputStream.write(tempBuffer , offset , size);
                        }
                        this.remainingSize -= (long) size;
                        offset += size;
                        length -= size;
                    } else {
                        byte data = tempBuffer[offset++];
                        this.write(data & 255);
                        length--;
                    }
                }
            }

            public void write(int data) throws IOException {
                // 实现同原代码
                if (outputStream != null) {
                    outputStream.write(data);
                    if (--remainingSize <= 0L) {
                        outputStream.close();
                        outputStream = null;
                        file.setLastModified(size);
                        if (append) {
                            file.setReadOnly();
                        }
                        remainingSize = 4L;
                    }
                } else {
                    byteArrayOutputStream.write(data);
                    if (--remainingSize <= 0L) {
                        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                        if (byteArrayOutputStream.size() == 4) {
                            remainingSize = (long) (inputStream.readInt() - 4);
                            if (remainingSize > 16384L) {
                                throw new IOException("Illegal directory stream");
                            }
                        } else {
                            inputStream.readInt();
                            boolean isAppend = inputStream.read() == 1;
                            append = inputStream.read() == 1;
                            size = getFileSize(inputStream , new Runnable() {
                                public void run() {
                                }
                            });
                            if (isAppend) {
                                remainingSize = getFileSize(inputStream , new Runnable() {
                                    public void run() {
                                    }
                                });
                            } else {
                                remainingSize = 4L;
                            }
                            String fileName = inputStream.readUTF();
                            String filePath = destinationPath + "/" + fileName;
                            File outputFile = new File(filePath);
                            file = outputFile;
                            if (isAppend) {
                                if (!fileName.equals(tempFileName) && tempFileName != null) {
                                    outputStream = new ByteArrayOutputStream();
                                } else {
                                    if (name != null) {
                                        filePath = destinationPath + "/" + name;
                                    }
                                    if (remainingSize == 0L) {
                                        new File(filePath).createNewFile();
                                        outputStream = null;
                                        remainingSize = 4L;
                                    } else {
                                        outputStream = new BufferedOutputStream(new FileOutputStream(filePath) , 1048576);
                                    }
                                }
                            } else {
                                boolean mkdirs = outputFile.mkdirs();
                                outputFile.setLastModified(size);
                                if (mkdirs && isAppend) {
                                    outputFile.setReadOnly();
                                }
                                outputStream = null;
                            }
                            byteArrayOutputStream.reset();
                        }
                    }
                }
            }
        };
    }
    private static long getFileSize(DataInputStream inputStream, Runnable runnable) throws IOException {
        long size = 4L;
        while (inputStream.read() != -1) {
            size++;
        }
        runnable.run();
        return size;
    }

}

