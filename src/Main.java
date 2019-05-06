import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.border.EtchedBorder;
/**
 *
 * @author Mauy
 */
public class Main extends JFrame
{

    public Main()
    {
        this.setBounds(450, 300, 450, 300);
        this.setTitle("ZIPPER");
        this.setJMenuBar(menuBar);
        JMenu menuFile = menuBar.add(new JMenu("File"));

        Action actionAdd = new BAction("Add", "Add to list", "ctrl Q", new ImageIcon("plus.gif"));
        Action actionRemove = new BAction("Remove","Remove element from list", "ctrl W", new ImageIcon("minus.gif"));
        Action actionZip = new BAction("Zip","Zip all elements from list", "ctrl E", new ImageIcon("pakuj20.gif"));

        JMenuItem menuAdd = menuFile.add(actionAdd);
        JMenuItem menuRemove = menuFile.add(actionRemove);
        JMenuItem menuZip = menuFile.add(actionZip);


        add = new JButton(actionAdd);
        rmv = new JButton(actionRemove);
        zip = new JButton(actionZip);
        JScrollPane scroll = new JScrollPane(list);
        list.setBorder(BorderFactory.createEtchedBorder(Color.lightGray, Color.WHITE));

        GroupLayout layout = new GroupLayout(this.getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(add)
                                .addComponent(rmv))
                        .addComponent(scroll, 200, 250, Short.MAX_VALUE))
                .addGap(50,50, Short.MAX_VALUE)
                .addComponent(zip));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(add)
                                .addComponent(rmv))
                        .addGap(20,20, 20)
                        .addComponent(scroll, 50, 100, Short.MAX_VALUE))
                .addGap(20,20, Short.MAX_VALUE)
                .addComponent(zip) );

        this.getContentPane().setLayout(layout);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("Zp.gif"));
        this.pack();
    }
    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    private DefaultListModel DLModel = new DefaultListModel() //@
    {
        @Override
        public void addElement(Object obj)
        {
            list.add(obj);
            super.addElement(((File)obj).getName());
        }
        @Override
        public Object get(int index)
        {
            return list.get(index);
        }
        @Override
        public Object remove(int index)
        {                             
                                        
            list.remove(index);           
            return super.remove(index);    
        }

        ArrayList list = new ArrayList(); 

    };
    private JList list = new JList(DLModel);
    private JMenuBar menuBar = new JMenuBar();
    private JButton add = new JButton();
    private JButton rmv = new JButton();
    private JButton zip = new JButton();
    private JFileChooser JFChooser = new JFileChooser();

    private class BAction extends AbstractAction //------------------------------------- Action!
    {
        public BAction(String name, String dsc, String keyShort)
        {
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, dsc);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyShort));
        }
        public BAction(String name, String dsc, String keyShort, Icon icon)
        {
            this(name, dsc,keyShort);
            this.putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("Add"))
                addToList();

            if (e.getActionCommand().equals("Remove"))
                rmvFromList();
            if (e.getActionCommand().equals("Zip"))
                createZipArchive();
        }
        private void addToList() //----------------------------------------------------- add
        {
            JFChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            JFChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            JFChooser.setMultiSelectionEnabled(true);

            int tmp = JFChooser.showDialog(rootPane, "Add to list");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File[] paths = JFChooser.getSelectedFiles();

                for (int i=0; i < paths.length; i++)
                    if(!sameName(paths[i].getPath()))
                        DLModel.addElement(paths[i]);
            }
        }
        private boolean sameName (String testedElement)
        {
            for (int i=0; i < DLModel.getSize(); i++)

                if (((File)DLModel.get(i)).getPath().equals(testedElement))
                    return true;

            return false;
        }
        private void rmvFromList() //--------------------------------------------------- Remove
        {
            int[] tmp = list.getSelectedIndices();
            for (int i = 0; i<tmp.length ; i++)
                DLModel.remove(tmp[i]-i);
        }
        private void createZipArchive() //---------------------------------------------- Create zip archive
        {
            JFChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            JFChooser.setSelectedFile(new File(System.getProperty("user.dir")+File.separator+"Archive.zip"));
            int tmp = JFChooser.showDialog(rootPane, "Compress");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {


                byte tmpData[] = new byte[BUFFOR];
                try
                {  //----------------------------------------------------------------- Create empty zip
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(JFChooser.getSelectedFile()), BUFFOR));

                    for (int i= 0; i <DLModel.getSize(); i++)
                    {
                        if ( !((File)DLModel.get(i)).isDirectory() )
                            zip(zOutS, (File)DLModel.get(i), tmpData, ((File)DLModel.get(i)).getPath());
                        else
                        {
                            writePaths((File)DLModel.get(i));

                            for(int j = 0; j<pathList.size(); j++)
                                zip(zOutS, (File)pathList.get(j), tmpData, ((File)DLModel.get(i)).getPath());

                            pathList.removeAll(pathList);
                        }

                    }

                    zOutS.close();
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void zip(ZipOutputStream zOutS, File toFilePath, byte[] tmpData, String basePath) throws IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(toFilePath), BUFFOR);

            zOutS.putNextEntry(new ZipEntry(toFilePath.getPath().substring(basePath.lastIndexOf(File.separator)+1)));

            int counter;
            while((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zOutS.write(tmpData, 0, counter);

            zOutS.closeEntry();

            inS.close();
        }
        public static final int BUFFOR = 1024;

        private void writePaths(File pathName)
        {
            String[] filesAndDirs = pathName.list();
            System.out.println("");

            System.out.println(pathName.getPath());


            for (int i=0; i < filesAndDirs.length; i++)
            {
                File p = new File(pathName.getPath(), filesAndDirs[i]);

                if (p.isFile())
                    pathList.add(p);

                if (p.isDirectory())
                {
                    writePaths(new File(p.getPath()));
                }
            }

        }
        ArrayList pathList = new ArrayList();

    }


}

