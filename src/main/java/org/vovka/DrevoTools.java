package org.vovka;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.prefs.Preferences;

public class DrevoTools extends JFrame {

    private static DrevoTools frame;

    public DrevoTools() {
        frame = this;

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel labelTitle = new JLabel("<html>&nbsp;<br>Выберите действие:<br>&nbsp;<br>&nbsp;</html>");
        labelTitle.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        JCheckBox checkSplitLastName = new JCheckBox("Отделить девичью фамилию");
        checkSplitLastName.setSelected(true);

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

            String fileForImport;
            String fileForExport;
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                fileForImport = selectedFile.getAbsolutePath();
                prefs.put(prefFileNameToConvert, fileForImport);
                //JOptionPane.showMessageDialog(frame, "Выбран: " + selectedFile.getAbsolutePath());

                fileChooser.setDialogTitle("Сохранить результат в:");
                fileChooser.setSelectedFile(new File(prefs.get(prefFileNameSaved, System.getProperty("user.home"))));
                result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    fileForExport = selectedFile.getAbsolutePath();
                    prefs.put(prefFileNameSaved, fileForExport);
                    //JOptionPane.showMessageDialog(frame, "Сохранён: " + selectedFile.getAbsolutePath());

                    File inFile = new File(fileForImport);
                    File outFile = new File(fileForExport);
                    String line;
                    int countLinesTransfered = 0;
                    int countLinesAdded = 0;
                    try (
                            InputStream inStream = Files.newInputStream(inFile.toPath());
                            OutputStream outStream = Files.newOutputStream(outFile.toPath())
                    ) {

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                        line = reader.readLine();
                        while (line != null) {
                            //https://ru.wikipedia.org/wiki/GEDCOM

                            boolean tranfered = false;
                            try {
                                String advString = "";

                                //1 NAME Наталья Владимировна /Белова (Никифорова)/
                                if (checkSplitLastName.isSelected() && line.startsWith("1 NAME ") && line.endsWith(")/")) {
                                    int indStartMarnm = line.indexOf(" /");
                                    int indEndMarnm = line.indexOf(" (", indStartMarnm);
                                    int indEndLastName = line.indexOf(")/", indEndMarnm);
                                    if (indStartMarnm > -1 && indEndMarnm > -1 && indEndLastName > -1) {
                                        String marriedName = line.substring(indStartMarnm + 2, indEndMarnm).trim();
                                        if (!marriedName.isEmpty()) {
                                            advString = "2 _MARNM " + marriedName;
                                        }
                                        //1 NAME Елена Евгеньевна / (Осинкина)/   - вот такое тоже оптимизируем
                                        line = line.substring(0, indStartMarnm) + " /" + line.substring(indEndMarnm + 2, indEndLastName) + "/";
                                    }
                                }

                                outStream.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
                                tranfered = true;

                                if (!advString.isEmpty()) {
                                    outStream.write((advString + "\r\n").getBytes(StandardCharsets.UTF_8));
                                    countLinesAdded++;
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
                                if (!tranfered) {
                                    outStream.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
                                }
                            }

                            countLinesTransfered++;
                            line = reader.readLine();
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        JOptionPane.showMessageDialog(frame, "Строк перенесено: " + countLinesTransfered + ", добавлено: " + countLinesAdded);
                    }
                }
            }

        });


        JButton buttonClose = new JButton("Закрыть");
        buttonClose.addActionListener(e -> frame.dispose());

        JPanel panelConvert = new JPanel();
        panelConvert.setLayout(new BoxLayout(panelConvert, BoxLayout.Y_AXIS));
        panelConvert.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(), new EmptyBorder(10, 10, 10, 10)));
        panelConvert.add(buttonConvert);
        panelConvert.add(checkSplitLastName);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(labelTitle)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(panelConvert)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonClose)
                )
        );

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelTitle)
                        .addComponent(panelConvert)
                        .addComponent(buttonClose)
                )
        );

    }

    public static void main(String[] args) {

        DrevoTools app = new DrevoTools();
        app.setTitle("Древо Жизни: Конвертер");
        URL url =  DrevoTools.class.getResource("/images/AgelongTree.png");
        if (url != null) {
            app.setIconImage(new ImageIcon(url).getImage());
        }
        app.setSize(400, 300);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        app.setLocation(dim.width/2-app.getSize().width/2, dim.height/2-app.getSize().height/2);
        app.setVisible(true);
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
