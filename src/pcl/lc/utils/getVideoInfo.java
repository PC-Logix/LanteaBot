package pcl.lc.utils;

import java.net.URLEncoder;

import org.json.JSONObject;
import org.pircbotx.Colors;

public class getVideoInfo {
	
	
	public static String getVideoSearch(String query, boolean data, boolean url) {
		HTTPQuery q = null;

		try {
			q = HTTPQuery.create("http://gdata.youtube.com/feeds/api/videos?max-results=1&v=2&alt=jsonc&q="+URLEncoder.encode(query,"UTF8"));
			q.connect(true,false);

			JSONObject jItem = new JSONObject(q.readWhole()).getJSONObject("data");
			q.close();

			if (jItem.getInt("totalItems")==0)
				return null;
			jItem = jItem.getJSONArray("items").getJSONObject(0);

			String vID = jItem.getString("id");
			String vUploader = jItem.getString("uploader");
			String vTitle = jItem.getString("title");
			int vDuration = jItem.getInt("duration");
			double vRating = jItem.has("rating") ? jItem.getDouble("rating") : -1;
			int vViewCount = jItem.getInt("viewCount");

			int iDh = vDuration/3600, iDm = (vDuration/60) % 60, iDs = vDuration % 60;

			return (data ? Colors.BOLD+vTitle+Colors.NORMAL+" | length "+Colors.BOLD+(vDuration >= 3600 ? iDh+"h " : "")+(vDuration >= 60 ? iDm+"m " : "")+iDs+"s"+Colors.NORMAL+" | rated "
				+(vRating != -1 ? Colors.BOLD+String.format("%.2f",vRating).replace(",",".")+"/5.00"+Colors.NORMAL+" | " : "")
				+Colors.BOLD+vViewCount+Colors.NORMAL+" view"+(vViewCount != 1 ? "s" : "")+" | by "+Colors.BOLD+vUploader+Colors.NORMAL
				+(url ? " | " : "") : "")+(url ? "http://youtu.be/"+vID : "");
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	
	public String returnVideoInfo(String vID) {
		HTTPQuery q = null;

		try {
			q = HTTPQuery.create("http://gdata.youtube.com/feeds/api/videos/"+URLEncoder.encode(vID,"UTF8")+"?v=2&alt=jsonc");
			q.connect(true,false);

			JSONObject jItem = new JSONObject(q.readWhole()).getJSONObject("data");
			q.close();

			String vUploader = jItem.getString("uploader");
			String vTitle = StringTools.unicodeParse(jItem.getString("title"));
			int vDuration = jItem.getInt("duration");
			double vRating = jItem.has("rating") ? jItem.getDouble("rating") : -1;
			int vViewCount = jItem.getInt("viewCount");

			int iDh = vDuration/3600, iDm = (vDuration/60) % 60, iDs = vDuration % 60;

			return vTitle+" | length "+(vDuration >= 3600 ? iDh+"h " : "")+(vDuration >= 60 ? iDm+"m " : "")+iDs+"s | rated "
				+(vRating != -1 ? String.format("%.2f",vRating).replace(",",".")+"/5.00 | " : "")+vViewCount+" view"+(vViewCount != 1 ? "s" : "") +" | by "+vUploader;
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
