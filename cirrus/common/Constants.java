package cirrus.common;

public class Constants {
	public static final String FILE = "file";
	public static final String URL = "url";
	public static final String OUTPUT_FOLDER = "../output/";
	public static final String DOWNLOADS_FOLDER = "../downloads/";	
	
	public static String getFileNameFromUrl(String url) {
		if (url.indexOf("/") >= 0)
			url = url.substring(url.lastIndexOf("/") + 1);
		
		return url;
	}
}
