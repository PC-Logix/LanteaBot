package pcl.lc.utils;

import java.net.URLEncoder;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pircbotx.Colors;

public class getVideoInfo { 
	
	
	public static String getVideoSearch(String query, boolean data, boolean url, String apiKey) {
		HTTPQuery q = null;
		try {
			System.out.println("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet%2Cstatistics&id=" + query + "&key=" + apiKey);
			q = HTTPQuery.create("https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet%2Cstatistics&id=" + query + "&key=" + apiKey);
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
			return (data ?Colors.NORMAL + Colors.BOLD+vTitle+Colors.NORMAL+" | length: "+Colors.BOLD + vDuration +Colors.NORMAL
					+" | Likes: " + Colors.GREEN + vLikes + Colors.NORMAL + " Dislikes: " + Colors.RED + vDislikes + Colors.NORMAL
					+" View" + Colors.NORMAL + (vViewCount != 1 ? "s: " : ": ") + Colors.BOLD + vViewCount + Colors.NORMAL + " | by " + Colors.BOLD + vUploader + Colors.NORMAL : "");
			
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
