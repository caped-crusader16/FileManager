
package filemanager;

import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class StartPage extends JFrame {
    JPanel contentPane;
    JLabel imageLabel = new JLabel();
    JLabel headerLabel = new JLabel();

    public StartPage() {
        try {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.contentPane = (JPanel) getContentPane();
            this.contentPane.setLayout(new BorderLayout());
            this.setSize(new Dimension(800, 600));
            this.setTitle("My File Maganer");
            this.headerLabel.setFont(new java.awt.Font("Comic Sans MS", Font.BOLD, 16));
            this.headerLabel.setText("Welcome to My File Manager");
            Random random = new Random();
            try{
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(200);                 
                    Color c=new Color(256*(random.nextInt()),256*(random.nextInt()),256*(random.nextInt()),256*(random.nextInt()));
                    headerLabel.setForeground(c);
                }
            }catch(Exception e){
                System.out.println("Exception occured..."+e);
            }
            contentPane.add(headerLabel, java.awt.BorderLayout.EAST);
            // add the image label
            ImageIcon ii = new ImageIcon(this.getClass().getResource("filemanager.jpg"));
            imageLabel.setIcon(ii);
            contentPane.add(imageLabel, java.awt.BorderLayout.CENTER);
            // show it
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new StartPage();
    }

}
