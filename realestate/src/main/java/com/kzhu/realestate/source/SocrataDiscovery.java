package com.kzhu.realestate.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kzhu.realestate.http.HttpFetcher;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/** Lists candidate datasets on a Socrata portal so their endpoints can be added to states.properties. */
public final class SocrataDiscovery {
  private SocrataDiscovery() {}

  public static void print(HttpFetcher http, String domain, String keyword) throws IOException {
    String url = "https://api.us.socrata.com/api/catalog/v1?only=dataset&limit=50&domains="
        + URLEncoder.encode(domain, StandardCharsets.UTF_8)
        + "&q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    JsonObject o = new JsonParser().parse(http.get(url, Collections.emptyMap())).getAsJsonObject();
    JsonArray results = o.getAsJsonArray("results");
    for (JsonElement e : results) {
      JsonObject r = e.getAsJsonObject();
      JsonObject res = r.getAsJsonObject("resource");
      System.out.println(res.get("name").getAsString());
      System.out.println("  endpoint: https://" + domain + "/resource/" + res.get("id").getAsString() + ".json");
      if (res.has("columns_field_name")) {
        System.out.println("  columns:  " + res.get("columns_field_name"));
      }
    }
    if (results.size() == 0) {
      System.out.println("No datasets found on " + domain + " for '" + keyword + "'");
    }
  }
}
