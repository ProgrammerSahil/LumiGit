import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class LumiGit{
  public static void main(String[] args){
    if(args.length == 0) return;
    if(args[0].equals("init")){
      init();
    }
    else if(args[0].equals("hash-object")){
      if(args.length <= 1){
        System.out.println("Please Provide Filename");
        return;
      }
      byte[] output = hash_objects(args[1]);

    }
    else if(args[0].equals("cat-file")){
      if(args.length <= 1 || args[1].length() != 40){
        System.out.println("Please Provide a valid hash");
        return;
      }
      cat_file(args[1]);
    }
    else if(args[0].equals("write-tree")){
      write_tree();
    }
  }



  private static void write_tree(){
    try(Stream<Path> a = Files.list(Paths.get("."))) {

      a.forEach(item -> {
        System.out.println(item);
      });

    }catch(IOException e){
      e.printStackTrace();
    }
  }




  private static void cat_file(String hash){
    String folder = hash.substring(0, 2);
    String filename = hash.substring(2);

    Path filePath = Paths.get(".LumiGit", "objects",folder, filename);

    try{
      byte[] content = Files.readAllBytes(filePath);

      int nullByteIndex = -1;
      for(int i=0; i<content.length; i++){
        if(content[i] == 0){
          nullByteIndex = i;
          break;
        }
      }

      String codeContent = new String(content, nullByteIndex+1,content.length-(nullByteIndex+1) , StandardCharsets.UTF_8);



      for(char a: codeContent.toCharArray()){
        System.out.print(a);
      }

    }catch(IOException e){
      e.printStackTrace();
    }
   // test with c8d7500b8e8cd2930568fcae41ddc288a5617540

  }

  private static byte[] hash_objects(String file){

    Path path = Paths.get(file);

    try{
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] fileBytes = Files.readAllBytes(path);

      String header = "blob "+fileBytes.length+"\0";
      byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

      byte[] objectBytes = new byte[headerBytes.length+fileBytes.length];
      System.arraycopy(headerBytes, 0, objectBytes, 0, headerBytes.length);
      System.arraycopy(fileBytes, 0, objectBytes, headerBytes.length, fileBytes.length);

      md.update(objectBytes);
      byte[] hashBytes = md.digest();

      StringBuilder hexString = new StringBuilder();
      for(byte b: hashBytes){
        String hex = Integer.toHexString(0xff & b);
        if(hex.length() == 1) hexString.append('0');
        hexString.append(hex);

      }

      String folder = hexString.toString().substring(0,2);
      String fileName = hexString.toString().substring(2);

      if(!Files.exists(Paths.get(".LumiGit"))){
        System.out.println("Initialize first");
        return new byte[1];
      }

      Path bytesPath = Paths.get(".LumiGit", "objects", folder);

      Files.createDirectories(bytesPath);
      Path filePath = Paths.get(".LumiGit", "objects",folder, fileName);

      Files.write(filePath, objectBytes);

      return hashBytes;

    } catch(IOException | NoSuchAlgorithmException e){
      e.printStackTrace();
      return new byte[1];
    }


  }

  private static void init(){
    Path path = Paths.get(".LumiGit");

    if(Files.exists(path)){
      System.out.println("Repository already exists");
      return;
    }

    System.out.println("Initializing Lumi Repository...");
    new File(".LumiGit").mkdirs();
    new File(".LumiGit/objects").mkdirs();
    new File(".LumiGit/refs").mkdirs();
  }

}
