import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import java.util.List;
import java.io.ByteArrayOutputStream;

public class LumiGit {
  public static void main(String[] args) {
    if (args.length == 0) return;
    if (args[0].equals("init")) {
      init();
    } else if (args[0].equals("hash-object")) {
      if (args.length <= 1) {
        System.out.println("Please Provide Filename");
        return;
      }
      byte[] output = hash_objects(args[1]);
    } else if (args[0].equals("cat-file")) {
      if (args.length <= 1 || args[1].length() != 40) {
        System.out.println("Please Provide a valid hash");
        return;
      }
      cat_file(args[1]);
    } else if (args[0].equals("write-tree")) {
      write_tree();
    }
  }

  private static void init() {
    Path path = Paths.get(".LumiGit");

    if (Files.exists(path)) {
      System.out.println("Repository already exists");
      return;
    }

    System.out.println("Initializing Lumi Repository...");
    new File(".LumiGit").mkdirs();
    new File(".LumiGit/objects").mkdirs();
    new File(".LumiGit/refs").mkdirs();
  }

  private static byte[] save_object(String type, byte[] data) throws NoSuchAlgorithmException, IOException {
    String header = type + " " + data.length + "\0";
    byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

    byte[] objectBytes = new byte[headerBytes.length + data.length];
    System.arraycopy(headerBytes, 0, objectBytes, 0, headerBytes.length);
    System.arraycopy(data, 0, objectBytes, headerBytes.length, data.length);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] hashBytes = md.digest(objectBytes);

    String hexString = bytesToHex(hashBytes);
    String folder = hexString.substring(0, 2);
    String fileName = hexString.substring(2);

    if (!Files.exists(Paths.get(".LumiGit"))) {
      System.out.println("Initialize first");
      return new byte[1];
    }

    Path bytesPath = Paths.get(".LumiGit", "objects", folder);
    Files.createDirectories(bytesPath);
    
    Path filePath = bytesPath.resolve(fileName);
    Files.write(filePath, objectBytes);

    return hashBytes;
  }

  private static byte[] hash_objects(String file) {
    try {
      byte[] fileBytes = Files.readAllBytes(Paths.get(file));
      return save_object("blob", fileBytes);
    } catch (IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
      return new byte[1];
    }
  }

  private static void write_tree() {
    try (Stream<Path> stream = Files.list(Paths.get("."))) {
      List<Path> filteredFiles = stream
              .filter(path -> !path.getFileName().toString().equals(".LumiGit"))
              .filter(path -> !path.getFileName().toString().equals(".gitignore"))
              .filter(path -> !path.getFileName().toString().equals("LumiGit.java"))
              .filter(path -> !path.getFileName().toString().endsWith(".class"))
              .filter(path -> !path.getFileName().toString().endsWith(".git"))
              .sorted()
              .toList();

      ByteArrayOutputStream treeBody = new ByteArrayOutputStream();

      for (Path file : filteredFiles) {
        String name = file.getFileName().toString();
        byte[] blobHash = hash_objects(name);
        
        treeBody.write("100644 ".getBytes(StandardCharsets.UTF_8));
        treeBody.write(name.getBytes(StandardCharsets.UTF_8));
        treeBody.write(0);
        treeBody.write(blobHash);
      }

      byte[] allTreeEntries = treeBody.toByteArray();
      byte[] treeHash = save_object("tree", allTreeEntries);
      
      System.out.println(bytesToHex(treeHash));

    } catch (IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private static void cat_file(String hash) {
    String folder = hash.substring(0, 2);
    String filename = hash.substring(2);

    Path filePath = Paths.get(".LumiGit", "objects", folder, filename);

    try {
      byte[] content = Files.readAllBytes(filePath);

      int nullByteIndex = -1;
      for (int i = 0; i < content.length; i++) {
        if (content[i] == 0) {
          nullByteIndex = i;
          break;
        }
      }

      String codeContent = new String(content, nullByteIndex + 1, content.length - (nullByteIndex + 1), StandardCharsets.UTF_8);
      System.out.print(codeContent);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}