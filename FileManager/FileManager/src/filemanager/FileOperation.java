package filemanager;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;

@SuppressWarnings("Duplicates")

public class FileOperation {
    private final String key = new String("Why so Serious...????");
    private String currentDir;

    public FileOperation(String currentDir) {
        try {
            this.currentDir = new File(currentDir).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            this.currentDir = "/";
        }
    }

    public static void main(String[] args) throws IOException {
        // TODO test each method

        FileOperation op = new FileOperation(".");
        
        String desktopPath = new String("C:\\Users\\admin");
        for (int i = 0; i < 5; i++) {
            op.autoMakeDir(desktopPath,"abc");
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[(int)Math.pow(2,20)];
        int len = in.read(b);
        while (len != -1) {
            out.write(b, 0, len);
            len = in.read(b);
        }
        in.close();
        out.close();
    }

    // tested
    private void decrypt(File src, File dest) {
        try {
            CryptoUtils.decrypt(key, src, dest);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    // tested
    private void encrypt(File src, File dest) {
        try {
            CryptoUtils.encrypt(key, src, dest);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    // tested
    public void encrypt(String src, String dest) {
        File[] files = stringPair2FilePair(src, dest, "encrypted");
        encrypt(files[0], files[1]);
    }

    // tested
    public void decrypt(String src, String dest) {
        File[] files = stringPair2FilePair(src, dest, "decrypted");
        decrypt(files[0], files[1]);
    }

    private String interpretPath(String path) {
        System.out.println("\nPath : "+path);
        System.out.println("\nCurrent Directory : "+currentDir);
//        if (path.startsWith("/")) { 
//            return path;                                        // if it is an absolute path
//        } else if (currentDir.endsWith("/")) { 
//            return currentDir + path;                           // if current dir has a trailing "/"
//        } else {
//            return currentDir +"/"+ path;                     // if current dir does not have a trailing "/"
//        }
        return path;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    private void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public File[] listFilesAndDir(String dirName) {
        if (dirName == null) {
            dirName = currentDir;
        }
        File dir = new File(interpretPath(dirName));
        return dir.listFiles((file) -> !file.isHidden());
    }

    public File[] listFilesAndDir() {
        File dir = new File(currentDir);
        return dir.listFiles((file) -> !file.isHidden());
    }

    private File[] stringPair2FilePair(String src, String dest, String suffix) {
        if (suffix.isEmpty()) {
            suffix = "new";
        }

        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }
        String[] srcSlices = src.split("/");
        String srcLastComponent = srcSlices[srcSlices.length - 1];
        String srcLastFilename;
        String srcLastExtension;
        if (srcLastComponent.contains(".")) {
            int index = srcLastComponent.lastIndexOf(".");
            srcLastFilename = srcLastComponent.substring(0, index);  // does not contain the dot
            srcLastExtension = srcLastComponent.substring(index, srcLastComponent.length());  // does contain the dot
        } else {
            srcLastFilename = srcLastComponent;
            srcLastExtension = "";
        }

        System.out.println(srcLastFilename);
        System.out.println(srcLastExtension);
        String xy[] = srcLastFilename.split("\\");
        if (dest.endsWith("/")) {
            dest += xy[xy.length-1];
        }
        System.out.println("dest : "+dest);
        dest = interpretPath(dest);
        System.out.println("Interpreted dest : "+dest);
        while (new File(dest + srcLastExtension).exists()) {
            dest = dest + " " + suffix;
        }
        dest = dest + srcLastExtension;

        File[] files = new File[2];
        String x = interpretPath(src);
        System.out.println("src : "+x);
        files[0] = new File(src);
        String y = interpretPath(dest);
        System.out.println("dest : "+y);
        files[1] = new File(dest);
        return files;
    }

    public void copy(String src, String dest) throws IOException {
        System.out.println("src : "+src+" dest : "+dest);
        File[] files = stringPair2FilePair(src, dest, "copy");
        copy(files[0], files[1]);
    }

    private void copy(File src, File dest) throws IOException {
        System.out.println("Copy src: "+src+"\nCopy Dest: "+dest);
        if (src.isDirectory()) { // copy recursively
            System.out.println("Copying directory recursively");
            copyTree(src.toPath(), dest.toPath());
        } else { // copy a single file
            System.out.println("copying single file");
            copy(new FileInputStream(src), new FileOutputStream(dest));
        }
    }

    public String cat(String fileName) {
        try {
            File file = new File(interpretPath(fileName));
            Reader reader = new FileReader(file);
            StringBuffer buffer = new StringBuffer();
            int c = reader.read();
            while (c != -1) {
                buffer.append((char) c);
                c = reader.read();
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void copyTree(Path src, Path dest) {
        try {
            Files.walk(src).forEach(s -> {
                try {
                    Path d = dest.resolve(src.relativize(s));
                    if (Files.isDirectory(s)) {
                        if (!Files.exists(d))
                            Files.createDirectory(d);
                        return;
                    }
                    Files.copy(s, d);// use flag to override existing
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean mkdir(String dirName) {
        File dir = new File(interpretPath(dirName));
        boolean success = false;
        try {
            if (dir.mkdir()) {
                success = true;
            } else {
                throw new DirectoryNotEmptyException(dir.getAbsolutePath());
            }
        } catch (DirectoryNotEmptyException e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean rm(String path) {
        return new File(interpretPath(path)).delete();
    }

    public String pwd() {
        String dir;
        File f = new File(currentDir);
        try {
            dir = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            dir = f.getAbsolutePath();
        }
        return dir;
    }

    public void cd(String dir) {
        File f = new File(interpretPath(dir));
        try {
            if (f.isDirectory()) {
                setCurrentDir(f.getCanonicalPath());
            } else {
                throw new NotDirectoryException(f.getAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("current Directory remained " + currentDir);
            e.printStackTrace();
        }
    }

    // tested
    private void zip(File src, File dest) {
        try {
            Zipper.zip(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tested
    private void unzip(File src, File dest) {
        Unzipper.unzip(src, dest);
    }

    // public zip method tested
    public void zip(String src, String dest) {
        File[] files = stringPair2FilePair(src, dest, "zipped");
        zip(files[0], files[1]);
    }

    // public unzip method tested
    public void unzip(String src, String dest) {
        File[] files = stringPair2FilePair(src, dest, "unzipped");
        unzip(files[0], files[1]);
    }

    boolean autoMakeDir(String file, String str) {
//        if (file.isFile()) {
//            System.out.println(file.toString() + " is a File, recursively dealing with its parent directory");
//            return autoMakeDir(file.getParentFile());
//        }
        // auto-generate names until not conflicting with existent folders/files
        
        String newFolderName = file + str;
        while (new File(newFolderName).exists()) {
            System.out.println(newFolderName + " exists, try another name");
            newFolderName = newFolderName + " new";
        }
        return mkdir(newFolderName);
    }
    
    public boolean mkFile(String dirName) {
        File dir = new File(interpretPath(dirName));
        boolean success = false;
        try {
            if (dir.exists()) {
                success = true;
            } else {
                throw new DirectoryNotEmptyException(dir.getAbsolutePath());
            }
        } catch (DirectoryNotEmptyException e) {
            e.printStackTrace();
        }
        return success;
    }
}
