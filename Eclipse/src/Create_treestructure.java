import java.io.File;

public class Create_treestructure {
    public void create_folder(String Path_to_Folder_Name) {
        String[] path_parsed;
        path_parsed = Path_to_Folder_Name.split("/",5);
        String current_path=".";

        for (int i=1; i<5;i++) {
            current_path = current_path + "/" +path_parsed[i];
            File New_folder = new File(current_path);
            New_folder.mkdir();
        }
    }
}