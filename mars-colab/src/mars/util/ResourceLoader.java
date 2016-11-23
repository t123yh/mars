package mars.util;

import java.io.InputStream;

public class ResourceLoader {
	public static InputStream loadResource(String resourceName) {
		String resourcePath = "/resource/" + resourceName;
		return ResourceLoader.class.getResourceAsStream(resourcePath);
	}
}
