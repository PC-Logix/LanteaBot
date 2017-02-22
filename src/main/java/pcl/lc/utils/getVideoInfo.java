package pcl.lc.utils;

import java.text.NumberFormat;
import java.time.ZonedDateTime;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.pircbotx.Colors;
import java.time.format.DateTimeFormatter;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class getVideoInfo { 
	
	
	public static String getVideoSearch(String query, boolean data, boolean url, String apiKey, String userHost) {
		YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
		        new HttpRequestInitializer() {
		            public void initialize(HttpRequest request) throws IOException {
		            }
		        }).setApplicationName("LanteaBot").build();
		try {
			final String videoId = query;
			YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails");
			videoRequest.setId(videoId);
			videoRequest.setKey(apiKey);
			VideoListResponse listResponse = videoRequest.execute();
			List<Video> videoList = listResponse.getItems();

			Video targetVideo = videoList.iterator().next();
			
			String vUploader = targetVideo.getSnippet().getChannelTitle();
			String vTitle = targetVideo.getSnippet().getTitle();
			
			PeriodFormatter formatter = ISOPeriodFormat.standard();
			Period p = formatter.parsePeriod(targetVideo.getContentDetails().getDuration());
			int s = p.toStandardSeconds().getSeconds();
			String vDuration = PeriodFormat.getDefault().print(new Period(0, 0, s, 0).normalizedStandard()).replace(" minutes", "m").replace(" minute", "m").replace(" and", "").replace(" seconds", "s").replace(" second", "s").replace(" hours", "h").replace(" hour", "h");

			BigInteger vLikes = targetVideo.getStatistics().getLikeCount();
			BigInteger vDislikes = targetVideo.getStatistics().getDislikeCount();
			BigInteger vViewCount = targetVideo.getStatistics().getViewCount();
			final ZonedDateTime dateTime = ZonedDateTime.parse(targetVideo.getSnippet().getPublishedAt().toString(), DateTimeFormatter.ISO_DATE_TIME);
			
			return (data ?Colors.NORMAL + Colors.BOLD+vTitle+Colors.NORMAL+" | length: "+Colors.BOLD + vDuration +Colors.NORMAL
					+" | Likes: " + Colors.GREEN + NumberFormat.getIntegerInstance().format(vLikes) + Colors.NORMAL + " Dislikes: " + Colors.RED + NumberFormat.getIntegerInstance().format(vDislikes) + Colors.NORMAL
					+" View" + Colors.NORMAL + (vViewCount.intValue() != 1 ? "s: " : ": ") + Colors.BOLD + NumberFormat.getIntegerInstance().format(vViewCount) + Colors.NORMAL + " | by " + Colors.BOLD + vUploader + Colors.NORMAL + " | Published On " + dateTime.getDayOfMonth() + "/" + dateTime.getMonthValue() + "/" + dateTime.getYear() : "");
			
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
}
