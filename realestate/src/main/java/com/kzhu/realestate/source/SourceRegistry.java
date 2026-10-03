package com.kzhu.realestate.source;

import com.kzhu.realestate.http.HttpFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Builds the configured sources from a properties file. Socrata sources are listed by id:
 * <pre>
 * foreclosure.CA.endpoint=...   (keyed by two-letter state code; --state CA selects it)
 * agent.CA.endpoint=...         (same layout)
 * court.courtlistener.enabled=true
 * </pre>
 */
public class SourceRegistry {
  private final Properties props;
  private final HttpFetcher http;
  private final String state; // two-letter code, or null for "all configured"

  public SourceRegistry(Properties props, HttpFetcher http) {
    this(props, http, null);
  }

  /** With a state, only that state's sources ({@code foreclosure.<ST>.endpoint}, ...) are used. */
  public SourceRegistry(Properties props, HttpFetcher http, String state) {
    this.props = props;
    this.http = http;
    this.state = state == null ? null : state.trim().toUpperCase();
  }

  /** Sources of {@code type} (foreclosure, agent) that have no endpoint for the requested state. */
  public boolean hasState(String type) {
    return state != null && props.getProperty(type + "." + state + ".endpoint") != null;
  }

  public List<ForeclosureDataSource> foreclosureSources() {
    List<ForeclosureDataSource> out = new ArrayList<>();
    for (String id : ids("foreclosure")) {
      String p = "foreclosure." + id;
      out.add(new SocrataForeclosureSource("foreclosure-" + id, client(p), new FieldMapping(props, p)));
    }
    return out;
  }

  public List<AgentComplaintDataSource> agentComplaintSources() {
    List<AgentComplaintDataSource> out = new ArrayList<>();
    for (String id : ids("agent")) {
      String p = "agent." + id;
      out.add(new SocrataAgentComplaintSource("agent-complaints-" + id, client(p), new FieldMapping(props, p)));
    }
    return out;
  }

  public List<CourtOrderDataSource> courtOrderSources() {
    List<CourtOrderDataSource> out = new ArrayList<>();
    if (Boolean.parseBoolean(props.getProperty("court.courtlistener.enabled", "true"))) {
      out.add(new CourtListenerOrderSource(http,
          props.getProperty("court.courtlistener.url", "https://www.courtlistener.com/api/rest/v4/search/"),
          props.getProperty("court.courtlistener.token"),
          props.getProperty("court.courtlistener.keyword", "foreclosure")));
    }
    return out;
  }

  private SocrataClient client(String prefix) {
    String endpoint = props.getProperty(prefix + ".endpoint");
    if (endpoint == null) {
      throw new IllegalArgumentException("Missing property " + prefix + ".endpoint");
    }
    return new SocrataClient(http, endpoint, props.getProperty(prefix + ".appToken"));
  }

  /** State mode: just the state's id. Otherwise the explicit `<type>.sources` list, else every `<type>.<id>.endpoint`. */
  private List<String> ids(String type) {
    List<String> ids = new ArrayList<>();
    if (state != null) {
      if (hasState(type)) {
        ids.add(state);
      }
      return ids;
    }
    String explicit = props.getProperty(type + ".sources");
    if (explicit != null) {
      for (String s : explicit.split(",")) {
        if (!s.trim().isEmpty()) {
          ids.add(s.trim());
        }
      }
      return ids;
    }
    String pre = type + ".", post = ".endpoint";
    for (String key : new java.util.TreeSet<>(props.stringPropertyNames())) {
      if (key.startsWith(pre) && key.endsWith(post) && key.length() > pre.length() + post.length()) {
        ids.add(key.substring(pre.length(), key.length() - post.length()));
      }
    }
    return ids;
  }
}
