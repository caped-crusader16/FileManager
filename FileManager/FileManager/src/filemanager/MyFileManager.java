package filemanager;

import filemanager.FileOperation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MyFileManager {
    
    public static final String APP_TITLE = "My File Manager";
    private Desktop desktop;                                //  Used to open/edit/print files.
    private FileSystemView fileSystemView;                  //  Provides nice icons and names for files.
    private File currentFile;                               //  currently selected File.
    private File copySrc;
    private FileOperation op;                               //  File operations logic
    private JPanel gui; 
    private JTree tree;                                     //  File-system tree.
    private DefaultMutableTreeNode lastSelectedNode;        

    /**
     * 
     */
    private JTable table;                                   // Table for Directory listing
    private JProgressBar progressBar;
    private FileTableModel fileTableModel;                  // Table model for Displaying the output File[] in proper format.
    private ListSelectionListener listSelectionListener;
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;

            //General Declarations.....

    private JLabel fileName;
    private JTextField path;
    private JLabel date;
    private JLabel size;
    private JCheckBox readable;
    private JCheckBox writable;
    private JCheckBox executable;
    private JRadioButton isDirectory;
    private JRadioButton isFile;
    

           //   Constructor to initialize certain elements...

    public MyFileManager() {
        fileSystemView = FileSystemView.getFileSystemView();
        desktop = Desktop.getDesktop();
        setTable();
        op = new FileOperation("/");
        createUIComponents();
    }

    private void setTable() {
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);

        listSelectionListener = lse -> {
            int row = table.getSelectionModel().getLeadSelectionIndex();
            setFileDetails(((FileTableModel) table.getModel()).getFile(row));
        };
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    public JPanel getGUI() {
        return gui;
    }
    
    public void setGUI(JPanel gui){
        this.gui = gui;
    }
    
                    //      File Details with GUI
    
    private void createUIComponents() {
        gui = new JPanel(new BorderLayout(3, 3));
        gui.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel detailView = new JPanel(new BorderLayout(3, 3));

        JScrollPane tableScroll = new JScrollPane(table);
        Dimension d = tableScroll.getPreferredSize();
        tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
        detailView.add(tableScroll, BorderLayout.CENTER);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);

        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                lastSelectedNode = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
                refreshTable();
            }
        };

        File[] roots = fileSystemView.getRoots();
        for (File fileSystemRoot : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
            root.add(node);
            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }
        }

        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(treeSelectionListener);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.expandRow(0);
        JScrollPane treeScroll = new JScrollPane(tree);
        tree.setVisibleRowCount(20);

        Dimension preferredSize = treeScroll.getPreferredSize();
        Dimension widePreferred = new Dimension(200, (int) preferredSize.getHeight());
        treeScroll.setPreferredSize(widePreferred);

        JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
        fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));
        JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);
        fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
        fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
        fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
        fileDetailsLabels.add(new JLabel("File size", JLabel.TRAILING));
        fileDetailsLabels.add(new JLabel("Type", JLabel.TRAILING));


        JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
        fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);
        fileName = new JLabel();
        fileDetailsValues.add(fileName);
        path = new JTextField(5);
        path.setEditable(false);
        fileDetailsValues.add(path);
        date = new JLabel();
        fileDetailsValues.add(date);
        size = new JLabel();
        fileDetailsValues.add(size);

        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));

        isDirectory = new JRadioButton("Directory");
        flags.add(isDirectory);

        isFile = new JRadioButton("File");
        flags.add(isFile);
        fileDetailsValues.add(flags);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        //                  Locate Button Section
        //                  It would locate the file or Folder in the File Explorer
        
        JButton locateFile = new JButton("Locate");
        locateFile.setMnemonic('l');

        locateFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    System.out.println("Locate: " + currentFile.getParentFile());
                    desktop.open(currentFile.getParentFile());
                } catch (Throwable t) {
                    showThrowable(t);
                }
                gui.repaint();
            }
        });
        toolBar.add(locateFile);
        
        //              Open Button.... 
        //              It would work as Locate for folders and it will open the file with supoortive file application
        
        JButton openFile = new JButton("Open");
        openFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    System.out.println("Open: " + currentFile);
                    desktop.open(currentFile);
                } catch (Throwable t) {
                    showThrowable(t);
                }
                gui.repaint();
            }
        });
        toolBar.add(openFile);
        
        //              Edit Button.... 
        //              It would work as Locate for folders and it will open the file with supoortive file application to edit the files

        JButton editFile = new JButton("Edit");
        editFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    if(currentFile.isDirectory()){
                        JOptionPane.showMessageDialog(gui, "Selection is not a file", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }else{
                        desktop.edit(currentFile);
                    }
                } catch (Throwable t) {
                    showThrowable(t);
                }
            }
        });
        toolBar.add(editFile);

        //          Copy Button Section
        //          Copy Files and functions...
        
        JButton copyFile = new JButton("Copy");
        copyFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                copySrc = currentFile;
            }
        });
        toolBar.add(copyFile);

        //              Paste Button Section
        //              Pastes the currently copied files
        
        JButton pasteFile = new JButton("Paste");
        pasteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String dest;
                try {
                    if (!currentFile.isDirectory()) {
                        dest = currentFile.getParent();
                        System.out.println("File : "+dest);
                    } else {
                        dest = currentFile.getCanonicalPath();
                        System.out.println("Directory : "+dest);
                    }
                    if (!dest.endsWith("/")) {
                        dest = dest + "/";
                        System.out.println("Final Dest : "+dest);
                    }
                    System.out.printf("Copying from %s to %s \n", copySrc.getCanonicalPath(), dest);
                    op.copy(copySrc.getCanonicalPath(), dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                refreshTable();
            }
        });
        toolBar.add(pasteFile);
        
        //          Searching module...
        
        JButton searchFile = new JButton("Search");
        searchFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String str = (String) JOptionPane.showInputDialog(new JFrame(), "Please enter Filename to be searched", "Search Dialoge-Box", JOptionPane.OK_CANCEL_OPTION);
                    SearchingFiles sf = new SearchingFiles();
                    sf.logic(str);
                } catch (Throwable t) {
                    showThrowable(t);
                }
                gui.repaint();
            }
        });
        toolBar.add(searchFile);

        // mkdir button debugged
        JButton mkdir = new JButton("Create Folder");
        mkdir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("making directory");
                String str = JOptionPane.showInputDialog(new JFrame(), "Please enter the directory name", "MK-DIR", JOptionPane.PLAIN_MESSAGE);
                boolean success = op.autoMakeDir(currentFile.getAbsolutePath(),str);
                System.out.println("New folder " + (success ? "made" : "not made"));
                refreshTable();
            }
        });
        toolBar.add(mkdir);
        
        // make files button debugged
        JButton mkfl = new JButton("Create Files");
        mkfl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Creating files");
                String str="";
                do{
                    str = JOptionPane.showInputDialog(new JFrame(), "Please enter the file name", "MK-DIR", JOptionPane.PLAIN_MESSAGE);
                }while (new File(currentFile.getAbsolutePath()+str).exists());
                
                boolean success = op.mkFile(currentFile.getAbsolutePath()+str);
                System.out.println("New folder " + (success ? "made" : "not made"));
                refreshTable();
            }
        });
        toolBar.add(mkfl);
    
                    //      Encryption button Section...
        
        JButton encryptFile = new JButton("Encrypt");
        encryptFile.addActionListener(new ActionListener() {
        
            @Override
            public void actionPerformed(ActionEvent ae) {
                String src;
                String dest;
                System.out.println("encrypting file" + currentFile.getPath());
                if (currentFile.isDirectory()) {
                    System.out.println("Selection is not a File");
                    if (copySrc.isDirectory()) {
                        JOptionPane.showMessageDialog(gui,"Selected file is neither a File...Aborting thr step","Error",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    System.out.println("Encrypting copySrc");
                    src = copySrc.getAbsolutePath();
                    dest = currentFile.getAbsolutePath();
                } else {
                    src = currentFile.getAbsolutePath();
                    dest = currentFile.getParentFile().getAbsolutePath();
                }
                op.encrypt(src, dest + "/");
                refreshTable();
            }
        });
        toolBar.add(encryptFile);

                            //      Decryption button Section... 
                            
        JButton decryptFile = new JButton("Decrypt");
        decryptFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("decrypting file" + currentFile.getPath());
                String src;
                String dest;
                if (currentFile.isDirectory()) {
                    System.out.println("Selection is not a File");
                    if (copySrc.isDirectory()) {
                        JOptionPane.showMessageDialog(gui,"Selected file is neither a File...Aborting thr step","Error",JOptionPane.ERROR_MESSAGE);
                        System.out.println("copySrc is neither a File");
                        System.out.println("Aborting");
                        return;
                    }
                    System.out.println("Decrypting copySrc");
                    src = copySrc.getAbsolutePath();
                    dest = currentFile.getAbsolutePath();
                } else {
                    src = currentFile.getAbsolutePath();
                    dest = currentFile.getParentFile().getAbsolutePath();
                }
                op.decrypt(src, dest + "/");
                refreshTable();
            }
        });
        toolBar.add(decryptFile);

        // zip button debugged
        JButton zipFile = new JButton("Zip");
        zipFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("zipping file" + currentFile.getPath());
                String src;
                String dest;
                if (!currentFile.isDirectory()) {
                    JOptionPane.showMessageDialog(gui,"Selected file is not a folder...Aborting thr step","Error",JOptionPane.ERROR_MESSAGE);
                    System.out.println("Selection is not a Folder");
                    if (!copySrc.isDirectory()) {
                        System.out.println("copySrc is neither a Foler");
                        System.out.println("Aborting");
                        return;
                    }
                    System.out.println("Zipping copySrc");
                    src = copySrc.getAbsolutePath();
                    dest = currentFile.getAbsolutePath();
                } else {
                    src = currentFile.getAbsolutePath();
                    dest = currentFile.getParent();
                }
                op.zip(src, dest + "/");
                refreshTable();
            }
        });
        toolBar.add(zipFile);

        // TODO further optimize the behaviour of the unzip button
        JButton unzipFile = new JButton("Unzip");
        unzipFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("unzipping file" + currentFile.getPath());
                String src;
                String dest;
                if (!currentFile.isFile()) {
                    System.out.println("Selection is not a zipped file");
                    JOptionPane.showMessageDialog(gui,"Selected file is neither a File...Aborting thr step","Error",JOptionPane.ERROR_MESSAGE);
                    if (!copySrc.isFile()) {
                        System.out.println("copySrc is neither a zipped file");
                        System.out.println("Aborting");
                        return;
                    }
                    System.out.println("Unzipping copySrc");
                    src = copySrc.getAbsolutePath();
                    dest = currentFile.getParent();
                } else {
                    src = currentFile.getAbsolutePath();
                    dest = currentFile.getParent();
                }
                op.unzip(src, dest + "/");
                refreshTable();
            }
        });
        toolBar.add(unzipFile);

        JButton deleteFile = new JButton("Delete");
        deleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("deleting file" + currentFile.getPath());
                if (currentFile.isDirectory() && currentFile.list().length != 0) {
                    System.out.println("The directory " + currentFile.getPath() + " is not empty");
                    System.out.println("Aborting");
                    return;
                }
                if (op.rm(currentFile.getPath())) {
                    System.out.println("Successfully deleted " + currentFile.getPath());
                } else {
                    System.out.println("Failed to delete " + currentFile.getPath());
                }
                refreshTable();
            }
        });
        toolBar.add(deleteFile);

        // Check the actions are supported on this platform!
        openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
        editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
        copyFile.setEnabled(true);
        pasteFile.setEnabled(true);
        mkdir.setEnabled(true);
        encryptFile.setEnabled(true);
        decryptFile.setEnabled(true);
        zipFile.setEnabled(true);
        unzipFile.setEnabled(true);
        deleteFile.setEnabled(true);

        flags.add(new JLabel("::  Flags"));
        readable = new JCheckBox("Read  ");
        readable.setMnemonic('a');
        flags.add(readable);

        writable = new JCheckBox("Write  ");
        writable.setMnemonic('w');
        flags.add(writable);

        executable = new JCheckBox("Execute");
        executable.setMnemonic('x');
        flags.add(executable);

        int count = fileDetailsLabels.getComponentCount();
        for (int ii = 0; ii < count; ii++) {
            fileDetailsLabels.getComponent(ii).setEnabled(false);
        }

        count = flags.getComponentCount();
        for (int ii = 0; ii < count; ii++) {
            flags.getComponent(ii).setEnabled(false);
        }

        JPanel fileView = new JPanel(new BorderLayout(3, 3));

        fileView.add(toolBar, BorderLayout.NORTH);
        fileView.add(fileMainDetails, BorderLayout.CENTER);

        detailView.add(fileView, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailView);
        gui.add(splitPane, BorderLayout.CENTER);

        JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
        progressBar = new JProgressBar();
        simpleOutput.add(progressBar, BorderLayout.EAST);
        progressBar.setVisible(false);

        gui.add(simpleOutput, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        showChildren(lastSelectedNode);
        setFileDetails((File) lastSelectedNode.getUserObject());
    }

    private void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(gui, t.toString(), t.getMessage(), JOptionPane.ERROR_MESSAGE);
        gui.repaint();
    }

    /**
     * Update the table on the EDT
     */
    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fileTableModel == null) {
                    fileTableModel = new FileTableModel();
                    table.setModel(fileTableModel);
                }
                table.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                table.getSelectionModel().addListSelectionListener(listSelectionListener);
                if (!cellSizesSet) {
                    Icon icon = fileSystemView.getSystemIcon(files[0]);

                    // size adjustment to better account for icons
                    table.setRowHeight(icon.getIconHeight() + rowIconPadding);

                    setColumnWidth(0, -1);
                    setColumnWidth(3, 60);
                    table.getColumnModel().getColumn(3).setMaxWidth(120);
                    setColumnWidth(4, -1);
                    setColumnWidth(5, -1);
                    setColumnWidth(6, -1);
                    setColumnWidth(7, -1);
                    setColumnWidth(8, -1);
                    setColumnWidth(9, -1);

                    cellSizesSet = true;
                }
            }
        });
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel((String) tableColumn.getHeaderValue());
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private void showChildren(final DefaultMutableTreeNode node) {
        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    File[] files = fileSystemView.getFiles(file, true); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                            }
                        }
                    }
                    setTableData(files);
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                tree.setEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Update the File details view with the details of this File.
     */
    private void setFileDetails(File file) {
        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);
        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        path.setText(file.getPath());
        date.setText(new Date(file.lastModified()).toString());
        size.setText(file.length() + " bytes");
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());

        isFile.setSelected(file.isFile());

        JFrame f = (JFrame) gui.getTopLevelAncestor();
        if (f != null) {
            f.setTitle(APP_TITLE + " :: " + fileSystemView.getSystemDisplayName(file));
        }

        gui.repaint();
    }

}
