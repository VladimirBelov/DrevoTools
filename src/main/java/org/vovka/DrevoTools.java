package org.vovka;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

public class DrevoTools extends JFrame {

    private static DrevoTools frame;
    private final JPanel panelMain;

    public DrevoTools() {
        frame = this;
        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));

        JLabel labelTitle = new JLabel("<html>&nbsp;<br>Выберите действие:<br>&nbsp;<br>&nbsp;</html>");
        labelTitle.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        panelMain.add(labelTitle);

        JButton buttonConvert = new JButton("Преобразовать для MyHerritage...");
        buttonConvert.addActionListener(e -> {
            Preferences prefs = Preferences.userNodeForPackage(DrevoTools.class);
            final String prefFileNameToConvert = "FileSelected";
            final String prefFileNameSaved = "FileSaved";

            //https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(prefs.get(prefFileNameToConvert, System.getProperty("user.home"))));
            fileChooser.setDialogTitle("Выберите GEDCOM файл:");

            class FileTypeFilter extends FileFilter {
                private final String extension;
                private final String description;

                public FileTypeFilter(String extension, String description) {
                    this.extension = extension;
                    this.description = description;
                }

                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    return file.getName().endsWith(extension);
                }

                public String getDescription() {
                    return description + String.format(" (*%s)", extension);
                }
            }
            FileFilter docFilter = new FileTypeFilter(".ged", "GEDCOM файлы");
            fileChooser.addChoosableFileFilter(docFilter);

            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                prefs.put(prefFileNameToConvert, selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(frame, "Выбран: " + selectedFile.getAbsolutePath());
            }

        });
        panelMain.add(buttonConvert, BorderLayout.NORTH);

        JButton buttonClose = new JButton("Закрыть");
        buttonClose.addActionListener(e -> frame.dispose());

        panelMain.add(buttonClose, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        DrevoTools app = new DrevoTools();
        app.setContentPane(app.panelMain);
        app.setTitle("Древо Жизни");
        //app.setIconImage(new ImageIcon("AgelongTree.gif").getImage());
        app.setSize(400, 300);
        app.setVisible(true);
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
