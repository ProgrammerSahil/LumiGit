import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

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
      hash_objects(args[1]);
    }
  }

  private static void hash_objects(String file){

    Path path = Paths.get(file);

    try{
      byte[] fileBytes = Files.readAllBytes(path);

      String header = "blob "+fileBytes.length+"\0";
      byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

      byte[] objectBytes = new byte[headerBytes.length+fileBytes.length];
      System.arraycopy(headerBytes, 0, objectBytes, 0, headerBytes.length);
      System.arraycopy(fileBytes, 0, objectBytes, headerBytes.length, fileBytes.length);

      System.out.println(headerBytes.length);
      System.out.println(fileBytes.length);
      System.out.println(objectBytes.length);

    } catch(IOException e){
      e.printStackTrace();
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
