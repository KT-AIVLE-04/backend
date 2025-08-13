package kt.aivle.sns.adapter.in.web.dto;

import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YoutubeAnalyticsResponse {
    private List<Map<String, Object>> rows;

    public YoutubeAnalyticsResponse(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public static YoutubeAnalyticsResponse from(QueryResponse response) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (response.getRows() != null) {
            for (List<Object> row : response.getRows()) {
                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < response.getColumnHeaders().size(); i++) {
                    rowMap.put(response.getColumnHeaders().get(i).getName(), row.get(i));
                }
                result.add(rowMap);
            }
        }

        return new YoutubeAnalyticsResponse(result);
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }
}
