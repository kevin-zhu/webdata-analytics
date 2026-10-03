package com.kzhu.realestate.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kzhu.realestate.http.HttpFetcher;
import com.kzhu.realestate.model.CourtOrderRecord;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Court orders from the CourtListener search API (https://www.courtlistener.com/help/api/rest/search/),
 * searching opinions (type=o). The query keyword defaults to "foreclosure". An API token is
 * optional but recommended (rate limits are much lower without one).
 */
public class CourtListenerOrderSource implements CourtOrderDataSource {
  private final HttpFetcher http;
  private final String baseUrl;
  private final String token;
  private final String defaultKeyword;

  public CourtListenerOrderSource(HttpFetcher http, String baseUrl, String token, String defaultKeyword) {
    this.http = http;
    this.baseUrl = baseUrl;
    this.token = token;
    this.defaultKeyword = defaultKeyword;
  }

  @Override public String name() { return "courtlistener"; }

  @Override
  public List<CourtOrderRecord> fetch(DataQuery q) throws IOException {
    Map<String, String> headers = new LinkedHashMap<>();
    if (token != null && !token.isEmpty()) {
      headers.put("Authorization", "Token " + token);
    }
    String keyword = q.keyword != null ? q.keyword : defaultKeyword;
    StringBuilder url = new StringBuilder(baseUrl).append("?type=o&order_by=dateFiled%20desc&q=")
        .append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
    if (q.sinceDate != null) {
      url.append("&filed_after=").append(URLEncoder.encode(q.sinceDate, StandardCharsets.UTF_8));
    }
    List<CourtOrderRecord> out = new ArrayList<>();
    String next = url.toString();
    while (next != null && out.size() < q.limit) {
      JsonObject page = new JsonParser().parse(http.get(next, headers)).getAsJsonObject();
      JsonArray results = page.has("results") ? page.getAsJsonArray("results") : new JsonArray();
      for (JsonElement e : results) {
        if (out.size() >= q.limit) {
          break;
        }
        out.add(toRecord(e.getAsJsonObject()));
      }
      JsonElement n = page.get("next");
      next = (n == null || n.isJsonNull()) ? null : n.getAsString();
    }
    return out;
  }

  private CourtOrderRecord toRecord(JsonObject o) {
    Map<String, String> raw = SocrataClient.flatten(o);
    CourtOrderRecord r = new CourtOrderRecord();
    r.caseNumber = raw.get("docketNumber");
    r.caseName = raw.get("caseName");
    r.court = raw.get("court");
    r.orderType = "opinion";
    r.orderDate = raw.get("dateFiled");
    r.judge = raw.get("judge");
    r.summary = raw.get("syllabus");
    String path = raw.get("absolute_url");
    r.url = path == null ? null : "https://www.courtlistener.com" + path;
    r.source = name();
    r.raw = raw;
    return r;
  }
}
