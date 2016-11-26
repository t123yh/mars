package mars.venus;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.luaj.vm2.LuaValue;

import mars.Globals;

public class SettingsLoadInstructionSetAction extends GuiAction {

	protected SettingsLoadInstructionSetAction(String name, Icon icon, String descrip, Integer mnemonic,
			KeyStroke accel, VenusUI gui) {
		super(name, icon, descrip, mnemonic, accel, gui);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(mainUI) == JFileChooser.APPROVE_OPTION) {
			String filepath = fileChooser.getSelectedFile().getPath();
			LuaValue chunk = Globals.getLuaBinding().getGlobals().loadfile(filepath);
			chunk.call();
			Globals.instructionSet.generateMatchMaps();
		}		
	}
}
