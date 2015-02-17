package pcl.lc.utils;

import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pircbotx.Colors;

public class getVideoInfo {
	
	
	public static String getVideoSearch(String query, boolean data, boolean url, String apiKey) {
		HTTPQuery q = null;

		
		try {
			q = HTTPQuery.create("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet%2Cstatistics&id=" + URLEncoder.encode(query,"UTF8") + "&key=" + apiKey);
			q.connect(true,false);
						
			JSONArray jItem = new JSONObject(q.readWhole()).getJSONArray("items");
			JSONObject snippet = jItem.getJSONObject(0).getJSONObject("snippet");
			JSONObject contentDetails = jItem.getJSONObject(0).getJSONObject("contentDetails");
			JSONObject statistics = jItem.getJSONObject(0).getJSONObject("statistics");
			
			if (jItem.length() < 0)
				return null;
			String vUploader = snippet.getString("channelTitle");
			String vTitle = snippet.getString("title");
			
			PeriodFormatter formatter = ISOPeriodFormat.standard();
			Period p = formatter.parsePeriod(contentDetails.getString("duration"));
			int s = p.toStandardSeconds().getSeconds();
			String vDuration = PeriodFormat.getDefault().print(new Period(0, 0, s, 0).normalizedStandard()).replace(" minutes", "m").replace(" minute", "m").replace(" and", "").replace(" seconds", "s").replace(" second", "s").replace(" hours", "h").replace(" hour", "h");

			int vLikes = statistics.has("likeCount") ? statistics.getInt("likeCount") : 0;
			int vDislikes = statistics.has("dislikeCount") ? statistics.getInt("dislikeCount") : 0;
			int vViewCount = statistics.getInt("viewCount");
			q.close();
			return (data ? Colors.BOLD+vTitle+Colors.NORMAL+" | length "+Colors.BOLD + vDuration +Colors.NORMAL
					+" | Likes: " + Colors.GREEN + vLikes + Colors.NORMAL + " Dislikes: " + Colors.RED + vDislikes + Colors.NORMAL
					+" View" + Colors.NORMAL + (vViewCount != 1 ? "s: " : ": ") + Colors.BOLD + vViewCount + " | by " + Colors.BOLD + vUploader + Colors.NORMAL : "");
			
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
