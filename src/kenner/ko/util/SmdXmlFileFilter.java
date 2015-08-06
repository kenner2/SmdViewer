package kenner.ko.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SmdXmlFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if(f.getName().endsWith(".smd") || f.getName().endsWith(".xml")){
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return ".smd and .xml";
	}

}
