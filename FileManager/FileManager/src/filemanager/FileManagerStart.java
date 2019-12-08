package filemanager;

import javax.swing.*;

public class FileManagerStart extends JFrame {
    public FileManagerStart() {
        super();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StartPage sp = new StartPage();
        try{
            Thread.sleep(2000);
        }catch(Exception e){
            System.out.println("Exception occured in FileManagerStart : "+e);
        }
        MyFileManager fileBrowser = new MyFileManager();
        this.setContentPane(fileBrowser.getGUI());
        this.pack();
//        this.setLocationByPlatform(true);
        this.setMinimumSize(this.getSize());
        this.setVisible(true);
        sp.setVisible(false);
    }


    public static void main(String[] args) {
        FileManagerStart fw = new FileManagerStart();
    }
}
