package com.kzhu.realestate.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/** Minimal GET-a-URL-return-body abstraction (so sources can be tested without a network). */
public interface HttpFetcher {
  String get(String url, Map<String, String> headers) throws IOException;

  /** Default implementation backed by java.net.http. Honors the JVM proxy settings. */
  static HttpFetcher defaultFetcher() {
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    return (url, headers) -> {
      HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
          .timeout(Duration.ofSeconds(60))
          .header("Accept", "application/json");
      headers.forEach(b::header);
      try {
        HttpResponse<String> r = client.send(b.GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) {
          throw new IOException("HTTP " + r.statusCode() + " from " + url);
        }
        return r.body();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted fetching " + url, e);
      }
    };
  }
}
