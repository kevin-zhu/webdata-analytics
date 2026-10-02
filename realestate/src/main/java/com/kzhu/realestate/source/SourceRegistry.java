package com.kzhu.realestate.source;

import com.kzhu.realestate.http.HttpFetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Builds the configured sources from a properties file. Socrata sources are listed by id:
 * <pre>
 * foreclosure.sources=ca_nod
 * foreclosure.ca_nod.endpoint=https://data.example.gov/resource/xxxx-yyyy.json
 * foreclosure.ca_nod.field.propertyAddress=site_address
 * agent.sources=...        (same layout, "agent.&lt;id&gt;...")
 * court.courtlistener.enabled=true
 * </pre>
 */
public class SourceRegistry {
  private final Properties props;
  private final HttpFetcher http;

  public SourceRegistry(Properties props, HttpFetcher http) {
    this.props = props;
    this.http = http;
  }

  public List<ForeclosureDataSource> foreclosureSources() {
    List<ForeclosureDataSource> out = new ArrayList<>();
    for (String id : ids("foreclosure.sources")) {
      String p = "foreclosure." + id;
      out.add(new SocrataForeclosureSource("foreclosure-" + id, client(p), new FieldMapping(props, p)));
    }
    return out;
  }

  public List<AgentComplaintDataSource> agentComplaintSources() {
    List<AgentComplaintDataSource> out = new ArrayList<>();
    for (String id : ids("agent.sources")) {
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

  private List<String> ids(String key) {
    List<String> ids = new ArrayList<>();
    for (String s : props.getProperty(key, "").split(",")) {
      if (!s.trim().isEmpty()) {
        ids.add(s.trim());
      }
    }
    return ids;
  }
}
