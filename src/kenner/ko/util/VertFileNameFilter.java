package kenner.ko.util;

import java.io.File;
import java.io.FilenameFilter;

public class VertFileNameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		if(name.substring(name.length()-5, name.length()).equalsIgnoreCase(".vert")){
			return true;
		}
		return false;
	}

}
