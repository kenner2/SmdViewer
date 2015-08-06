package kenner.ko.util;

import java.io.File;
import java.io.FilenameFilter;

public class FragFileNameFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		if(name.substring(name.length()-5, name.length()).equalsIgnoreCase(".frag")){
			return true;
		}
		return false;
	}

}
