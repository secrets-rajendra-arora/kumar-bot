
package ai.kumar.server.api.kumar;

import java.util.List;

import org.broadbear.link.preview.SourceContent;
import org.broadbear.link.preview.TextCrawler;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.mind.KumarThought;
import ai.kumar.server.APIException;
import ai.kumar.server.APIHandler;
import ai.kumar.server.AbstractAPIHandler;
import ai.kumar.server.Authorization;
import ai.kumar.server.BaseUserRole;
import ai.kumar.server.ClientConnection;
import ai.kumar.server.Query;
import ai.kumar.server.ServiceResponse;

import javax.servlet.http.HttpServletResponse;

public class RSSReaderService extends AbstractAPIHandler implements APIHandler {

	private static final long serialVersionUID = 1463185662941444503L;

    @Override
    public BaseUserRole getMinimalBaseUserRole() { return BaseUserRole.ANONYMOUS; }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }

    public String getAPIPath() {
        return "/kumar/rssreader.json";
    }
    
    @Override
    public ServiceResponse serviceImpl(Query post, HttpServletResponse response, Authorization rights, final JsonObjectWithDefault permissions) throws APIException {
		String url = post.get("url", "");
		return new ServiceResponse(readRSS(url).toJSON());
    }
		
    @SuppressWarnings("unchecked")
    public static KumarThought readRSS(String url) {
    	
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = null;
        
		try {
            ClientConnection connection = new ClientConnection(url);
		    XmlReader xmlreader = new XmlReader(connection.inputStream);
			feed = input.build(xmlreader);
		} catch (Exception e) {
			e.printStackTrace();
			return new KumarThought(); // fail
		}

		@SuppressWarnings("unused")
		int totalEntries = 0;
		int i = 0;

		JSONArray jsonArray = new JSONArray();

		// Reading RSS Feed from the URL
		for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("title", entry.getTitle().toString());
			jsonObject.put("link", entry.getLink().toString());
			jsonObject.put("uri", entry.getUri().toString());
			jsonObject.put("guid", Integer.toString(entry.hashCode()));
			SourceContent sourceContent = 	TextCrawler.scrape(entry.getUri(),3);
			if (entry.getPublishedDate() != null) jsonObject.put("pubDate", entry.getPublishedDate().toString());
			if (entry.getUpdatedDate() != null) jsonObject.put("updateDate", entry.getUpdatedDate().toString());
			if (entry.getDescription() != null) jsonObject.put("description", entry.getDescription().getValue().toString());
			if (sourceContent.getImages() != null) jsonObject.put("image", sourceContent.getImages().get(0));
			if (sourceContent.getDescription() != null) jsonObject.put("descriptionShort", sourceContent.getDescription());

			jsonArray.put(i, jsonObject);

			i++;
		}

		totalEntries = i;

		KumarThought json = new KumarThought();
		json.setData(jsonArray);
		return json;
	}
}
