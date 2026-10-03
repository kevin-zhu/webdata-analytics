package com.kzhu.realestate.app;

import com.kzhu.realestate.http.HttpFetcher;
import com.kzhu.realestate.source.DataQuery;
import com.kzhu.realestate.source.RealEstateDataSource;
import com.kzhu.realestate.source.SocrataDiscovery;
import com.kzhu.realestate.source.SourceRegistry;
import com.kzhu.realestate.store.JsonFileStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Pulls foreclosure, agent complaint/citation and court order data and writes one JSON file per source.
 *
 * <pre>
 * Usage: RealEstateDataApp [--config file] [--out dir] [--type foreclosure,agent,court]
 *                          [--state XX] [--discover portal-domain] [--keyword text] [--since yyyy-MM-dd] [--limit N]
 * </pre>
 */
public class RealEstateDataApp {
  private static void warnIfMissing(String state, String type, SourceRegistry registry) {
    if (state != null && !registry.hasState(type)) {
      System.err.println("No " + type + " endpoint configured for state " + state.toUpperCase()
          + " (add " + type + "." + state.toUpperCase() + ".endpoint to states.properties or your config)");
    }
  }

  public static void main(String[] args) throws IOException {
    Path config = null;
    Path out = Paths.get("realestate-data");
    Set<String> types = new HashSet<>(List.of("foreclosure", "agent", "court"));
    String state = null, keyword = null, since = null, discover = null;
    int limit = 1000;

    for (int i = 0; i < args.length; i++) {
      String a = args[i];
      if (a.equals("--help") || i + 1 >= args.length) {
        System.err.println("Usage: RealEstateDataApp [--config file] [--out dir] [--type foreclosure,agent,court] "
            + "[--state XX] [--discover portal-domain] [--keyword text] [--since yyyy-MM-dd] [--limit N]");
        System.exit(a.equals("--help") ? 0 : 2);
      }
      String v = args[++i];
      switch (a) {
        case "--config": config = Paths.get(v); break;
        case "--out": out = Paths.get(v); break;
        case "--type": types = new HashSet<>(List.of(v.split(","))); break;
        case "--state": state = v; break;
        case "--keyword": keyword = v; break;
        case "--since": since = v; break;
        case "--discover": discover = v; break;
        case "--limit": limit = Integer.parseInt(v); break;
        default:
          System.err.println("Unknown option " + a);
          System.exit(2);
      }
    }

    if (discover != null) {
      SocrataDiscovery.print(HttpFetcher.defaultFetcher(), discover, keyword == null ? "foreclosure" : keyword);
      return;
    }

    // Bundled per-state endpoint registry first; the user's config file overrides/extends it.
    Properties props = new Properties();
    try (InputStream in = RealEstateDataApp.class.getResourceAsStream("/states.properties")) {
      if (in != null) {
        props.load(in);
      }
    }
    if (config != null) {
      if (!Files.isRegularFile(config)) {
        System.err.println("Config file not found: " + config.toAbsolutePath()
            + "\nCopy realestate/conf/realestate.properties.sample to realestate.properties and edit it.");
        System.exit(2);
      }
      try (Reader r = Files.newBufferedReader(config)) {
        props.load(r);
      }
    }
    SourceRegistry registry = new SourceRegistry(props, HttpFetcher.defaultFetcher(), state);
    List<RealEstateDataSource<?>> sources = new ArrayList<>();
    if (types.contains("foreclosure")) {
      sources.addAll(registry.foreclosureSources());
      warnIfMissing(state, "foreclosure", registry);
    }
    if (types.contains("agent")) {
      sources.addAll(registry.agentComplaintSources());
      warnIfMissing(state, "agent", registry);
    }
    if (types.contains("court")) sources.addAll(registry.courtOrderSources());

    DataQuery query = DataQuery.of(state, keyword, since, limit);
    JsonFileStore store = new JsonFileStore(out);
    int failures = 0;
    for (RealEstateDataSource<?> s : sources) {
      try {
        List<?> records = s.fetch(query);
        Path file = store.save(s.name(), records);
        System.out.println(s.name() + ": " + records.size() + " records -> " + file);
      } catch (IOException | RuntimeException e) {
        failures++;
        System.err.println(s.name() + ": FAILED - " + e);
      }
    }
    if (sources.isEmpty()) {
      System.err.println("No sources configured; pass --config (see realestate/conf/realestate.properties.sample)");
    }
    if (failures > 0 || sources.isEmpty()) {
      System.exit(1);
    }
  }
}
