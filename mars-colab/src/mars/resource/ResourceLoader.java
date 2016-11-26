package mars.resource;

import java.io.InputStream;

public class ResourceLoader {
	public static InputStream loadResource(String resourceName) {
		return ResourceLoader.class.getResourceAsStream(resourceName);
	}
}
