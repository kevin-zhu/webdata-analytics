package com.kzhu.realestate.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kzhu.realestate.http.HttpFetcher;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads rows from a Socrata (SODA) open-data endpoint, e.g.
 * {@code https://data.example.gov/resource/abcd-1234.json}. Most state / city foreclosure and
 * license-discipline datasets are published this way. Pages through results with $limit/$offset.
 */
public class SocrataClient {
  private static final int PAGE_SIZE = 1000;

  private final HttpFetcher http;
  private final String endpoint;
  private final String appToken; // optional

  public SocrataClient(HttpFetcher http, String endpoint, String appToken) {
    this.http = http;
    this.endpoint = endpoint;
    this.appToken = appToken;
  }

  /**
   * @param where optional SoQL $where clause, may be null
   * @param q optional full text search, may be null
   */
  public List<Map<String, String>> rows(String where, String q, int limit) throws IOException {
    List<Map<String, String>> out = new ArrayList<>();
    Map<String, String> headers = new LinkedHashMap<>();
    if (appToken != null && !appToken.isEmpty()) {
      headers.put("X-App-Token", appToken);
    }
    int offset = 0;
    while (out.size() < limit) {
      int page = Math.min(PAGE_SIZE, limit - out.size());
      StringBuilder url = new StringBuilder(endpoint).append(endpoint.contains("?") ? '&' : '?')
          .append("$limit=").append(page).append("&$offset=").append(offset);
      if (where != null && !where.isEmpty()) {
        url.append("&$where=").append(enc(where));
      }
      if (q != null && !q.isEmpty()) {
        url.append("&$q=").append(enc(q));
      }
      JsonArray arr = new JsonParser().parse(http.get(url.toString(), headers)).getAsJsonArray();
      for (JsonElement e : arr) {
        out.add(flatten(e.getAsJsonObject()));
      }
      if (arr.size() < page) {
        break;
      }
      offset += page;
    }
    return out;
  }

  static Map<String, String> flatten(JsonObject o) {
    Map<String, String> m = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> e : o.entrySet()) {
      JsonElement v = e.getValue();
      if (v.isJsonNull()) {
        continue;
      }
      m.put(e.getKey(), v.isJsonPrimitive() ? v.getAsString() : v.toString());
    }
    return m;
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
