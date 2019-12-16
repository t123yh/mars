package mars.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mars.Globals;
import mars.ProgramStatement;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.mips.hardware.Register;
import mars.mips.hardware.RegisterAccessNotice;
import mars.mips.hardware.RegisterFile;

@SuppressWarnings("serial")
public class SystemMonitor extends AbstractMarsToolAndApplication {

    private static final String name = "System Monitor";
    private static final String version = "0.1";

    private JButton wSaveToFile;
    private JPanel topPanel;
    private JPanel masterPanel;
    private JScrollPane scrollPane;
    private JTextArea textArea;

    public SystemMonitor(String title, String heading) {
        super(title, heading);
    }

    @Override
    protected void addAsObserver() {
        RegisterFile.getProgramCounterRegister().addObserver(this);
        RegisterFile.addRegistersObserver(this);
        // don't observe PC since it's unsuitable for line-by-line comparison
        // RegisterFile.getPCRegister().addObserver(this);
        try {
            // thanks to Java's forced signed int you can't cross the 0x80000000
            // boundary
            Globals.memory.addObserver(this, 0, 0x80000000 - Memory.WORD_LENGTH_BYTES);
        } catch (AddressErrorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void deleteAsObserver() {
        RegisterFile.getProgramCounterRegister().deleteObserver(this);
        RegisterFile.deleteRegistersObserver(this);
        Globals.memory.deleteObserver(this);
    }

    @Override
    public void update(Observable resource, Object arg) {
        AccessNotice notice = (AccessNotice) arg;
        if (notice.getAccessType() == AccessNotice.WRITE) {
            if (notice instanceof RegisterAccessNotice) {
                Register reg = (Register) resource;
                if (reg.getNumber() == RegisterFile.PC) {
                    int pc = reg.getValue();
                    ProgramStatement stmt;
                    try {
                        stmt = Globals.memory.getStatement(pc);
                        if (stmt != null) {
                            textArea.append(
                                    String.format("@%08x %3d:\t%s\n", pc, stmt.getSourceLine(), stmt.getSource()));
                        }
                    } catch (AddressErrorException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    String regname;
                    switch (reg.getNumber()) {
                        case 33:
                            regname = "%HI";
                            break;
                        case 34:
                            regname = "%LO";
                            break;
                        default:
                            regname = String.format("$%2d", reg.getNumber());
                    }
                    textArea.append(String.format("%s <= %08x\n", regname, reg.getValue()));
                }
            } else if (notice instanceof MemoryAccessNotice) {
                MemoryAccessNotice mn = (MemoryAccessNotice) notice;
                textArea.append(
                        String.format("*%08x <= %0" + mn.getLength() * 2 + "x\n", mn.getAddress(), mn.getValue()));
            }
        }
    }

    // nullary ctor needed or won't be shown in Tools menu
    public SystemMonitor() {
        super(name + " " + version, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void reset() {
        textArea.setText(null);
    }

    @Override
    protected JComponent buildMainDisplayArea() {
        textArea = new JTextArea();
        // FIXME hardcoded font
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        // Scroll pane with text area
        scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        masterPanel = new JPanel(new BorderLayout());

        // Save to File button
        wSaveToFile = new JButton("Save to File");
        wSaveToFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                if (fc.showDialog(Globals.getGui(), null) != JFileChooser.APPROVE_OPTION)
                    return;
                File file = fc.getSelectedFile();
                FileWriter writer;
                try {
                    writer = new FileWriter(file);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
                try {
                    writer.write(textArea.getText());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        masterPanel.add(scrollPane);

        topPanel = new JPanel();
        topPanel.add(wSaveToFile);
        masterPanel.add(topPanel, BorderLayout.NORTH);
        return masterPanel;
    }
}