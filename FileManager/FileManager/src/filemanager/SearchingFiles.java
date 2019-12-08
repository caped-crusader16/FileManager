/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filemanager;

/**
 *
 * @author admin
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchingFiles {
    private String fileNameToSearch;
    private List<String> result = new ArrayList<String>();

    public String getFileNameToSearch() {
        return fileNameToSearch;
    }

    public void setFileNameToSearch(String fileNameToSearch) {
        this.fileNameToSearch = fileNameToSearch;
    }

    public List<String> getResult() {
        return result;
    }

    public static void logic(String file){
        SearchingFiles fileSearch = new SearchingFiles();
        fileSearch.searchDirectory(new File("C:\\Users\\admin"), file);
        int count = fileSearch.getResult().size();
        if (count == 0) {
            System.out.println("\nNo result found!");
        } else {
            for (String matched : fileSearch.getResult()) {
                System.out.println("Found : " + matched);
            }
        }
    }
    public static void main(String[] args) {
        logic("build.xml");
    }

    public void searchDirectory(File directory, String fileNameToSearch) {
        setFileNameToSearch(fileNameToSearch);
        if (directory.isDirectory()) {
            search(directory);
        } else {
            System.out.println(directory.getAbsoluteFile() + " is not a directory!");
        }

    }
    int count=0;
    private void search(File file) {
        if (file.isDirectory()) {
            count++;
            System.out.println(count + ".>  Searching directory ... " + file.getAbsoluteFile());
            //do you have permission to read this directory?	
            if (file.canRead()) {
                String dummy[] = file.list();
                if(dummy!=null){
                    for (File temp : file.listFiles()) {
                        if (temp.isDirectory()) {
                            search(temp);
                        } else {
                            if (getFileNameToSearch().equals(temp.getName().toLowerCase())) {
                                result.add(temp.getAbsoluteFile().toString());
                            }
                        }
                    }
                }else{
                    System.out.println("Directory "+file.getName()+" is empty..");
                }
            } else {
                System.out.println(file.getAbsoluteFile() + "Permission Denied");
            }
        }
    }
}
