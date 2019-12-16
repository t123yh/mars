package mars.venus;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mars.Globals;
import mars.Settings;

public class SettingsUseSystemLookAndFeelAction extends GuiAction {

    public SettingsUseSystemLookAndFeelAction(String name, Icon icon, String descrip, Integer mnemonic, KeyStroke accel,
                                              VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }

    public void actionPerformed(ActionEvent e) {
        VenusUI ui = Globals.getGui().mainUI;
        boolean b = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        Globals.getSettings().setBooleanSetting(Settings.USE_SYSTEM_LOOK_AND_FEEL, b);
        try {
            if (b) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            SwingUtilities.updateComponentTreeUI(ui);
            ui.pack();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
