package famousWebSite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetBingPages {
	private final static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
	private final static String fileDirAddr = "C:\\Users\\DaiQing\\Pictures\\bing\\";
	private final static String BingUrl = "http://cn.bing.com/";

	// get web page
	public static String getHtml(String url) throws IOException, InterruptedException {
		String html = null;
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).addHeader("User-Agent", userAgent).build();
		Response response = null;
		while (true) {
			try {
				response = client.newCall(request).execute();
				Pattern pattern = Pattern.compile("CMCCWLANFORM");
				html = response.body().string();
				Matcher matcher = pattern.matcher(html);
				if (matcher.find())
					throw new Exception();
			} catch (Exception e) {
				// sleep 3 minutes
				System.out.println("i'm going to sleep");
				// Thread.sleep(1000 * 60 * 3);
				Thread.sleep(1000 * 3);
				continue;
			}
			if (response.isSuccessful())
				break;
		}
		if (!response.isSuccessful())
			System.out.println("Network error");
		// System.out.println(Jsoup.parse(html));
		return html;
	}

	// get image name
	public static String getFileName(Document doc) throws Exception {
		String fileNameTmp = "";
		String fileName = "";
		Pattern pTitle = Pattern.compile("<a id=\"sh_cp\" class=\"sc_light\" title=\"(.*)\" a");
		Matcher mTitle = pTitle.matcher(doc.body().toString());
		if (mTitle.find()) {
			System.out.println(mTitle.group(1));
			fileNameTmp += mTitle.group(1);
			for (int i = 0; i < fileNameTmp.length(); ++i) {
				char nowCh = fileNameTmp.charAt(i);
				if (nowCh == '/' || nowCh == ':' || nowCh == '?' || nowCh == '*' || nowCh == '\"' || nowCh == '<'
						|| nowCh == '>' || nowCh == '|') {
					fileName += " ";
				} else {
					fileName += nowCh;
				}
			}
		} else
			throw new Exception("get image name error");
		return fileName;
	}

	// check file is downloaded or not
	private static boolean checkFileExist(String fileName) {
		File file = new File(fileDirAddr + fileName + ".jpg");
		if (file.exists()) {
			System.out.println("file exists");
			return true;
		}
		return false;
	}

	// use url to download
	/**
	 * 
	 * @param doc
	 * @param imageUrl
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private static String downloadJpg(Document doc, String imageUrl, String fileName) throws Exception {
		Pattern p = Pattern.compile("background-image:url\\((.*\\.jpg)");
		Matcher m = p.matcher(doc.toString());
		if (m.find()) {
			imageUrl += m.group(1);
			System.out.println(imageUrl);
			// downloading......
			try {
				download(imageUrl, fileDirAddr + fileName + ".jpg");
			} catch (Exception e) {
				// the png is not in the bing server
				download("http:" + m.group(1).toString(), fileDirAddr + fileName + ".jpg");
			}
			return imageUrl;
		}
		throw new Exception("get imageUrl error, download failed.");
	}

	private static void downLoadVideo(Document doc, String imageUrl, String fileName) throws Exception {

		Pattern p = Pattern.compile("g_vid = \\[\\[ \"(.*)\", \"(.*)\"(.*)\"\", \"(.*)\" \\]");
		Matcher m = p.matcher(doc.toString());
		if (m.find()) {

			imageUrl += m.group(4);

			// downloading......
			try {
				// System.out.println("video downloading 1");
				// System.out.println("image url:" + imageUrl);
				String videoUrl = "";
				for (int i = 0; i < imageUrl.length(); ++i) {
					if (imageUrl.charAt(i) != '\\')
						videoUrl += imageUrl.charAt(i);
				}
				// System.out.println("videoUrl url:" + videoUrl);
				download(videoUrl, fileDirAddr + fileName + ".mp4");
			} catch (Exception e) {
				String tmp = m.group(4).toString();
				String videoUrl = "";
				for (int i = 0; i < tmp.length(); ++i) {
					if (tmp.charAt(i) != '\\')
						videoUrl += tmp.charAt(i);
				}
				// System.out.println("catch video exception try video downloading again");
				// System.out.println("videoUrl url:" + "http:" + videoUrl);
				// the mp4 is not in the bing server
				download("http:" + videoUrl, fileDirAddr + fileName + ".mp4");
			}
		} else {
			System.out.println("the video do not exist");
		}
	}

	// get Url and Download download video
	public static String getImageUrl(String html) throws Exception {
		String imageUrl = "http://cn.bing.com";
		Document doc = Jsoup.parse(html);
		String fileName = getFileName(doc);
		if (checkFileExist(fileName))
			return "";
		try {
			downLoadVideo(doc, imageUrl, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return downloadJpg(doc, imageUrl, fileName);
		}
	}

	// download image
	public static void download(String urlString, String filename) throws Exception {
		URL url = new URL(urlString);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", userAgent);
		InputStream is = con.getInputStream();
		byte[] bs = new byte[1024 * 1024 * 100];
		int len;
		OutputStream os = new FileOutputStream(filename);
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		os.close();
		is.close();
	}

	public static void main(String[] args) throws Exception {
		getImageUrl(getHtml("http://cn.bing.com/"));
	}
}
