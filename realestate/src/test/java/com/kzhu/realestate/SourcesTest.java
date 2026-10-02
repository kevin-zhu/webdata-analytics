package com.kzhu.realestate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kzhu.realestate.model.AgentComplaintRecord;
import com.kzhu.realestate.model.CourtOrderRecord;
import com.kzhu.realestate.model.ForeclosureRecord;
import com.kzhu.realestate.source.*;
import com.kzhu.realestate.store.JsonFileStore;
import com.kzhu.realestate.http.HttpFetcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

public class SourcesTest {
  private static final String FORECLOSURES =
      "[{\"case_no\":\"A1\",\"site_address\":\"1 Main St\",\"state\":\"CA\",\"filing_date\":\"2026-01-02\"},"
      + "{\"case_no\":\"A2\",\"site_address\":\"2 Oak Ave\",\"state\":\"CA\"}]";

  @Test
  public void foreclosureMappingAndWhere() throws Exception {
    String[] seen = new String[1];
    HttpFetcher http = (url, h) -> { seen[0] = url; return FORECLOSURES; };
    Properties p = new Properties();
    p.setProperty("f.field.caseNumber", "case_no");
    p.setProperty("f.field.propertyAddress", "site_address");
    p.setProperty("f.field.filingDate", "filing_date");
    p.setProperty("f.stateColumn", "state");
    SocrataForeclosureSource s = new SocrataForeclosureSource("fc",
        new SocrataClient(http, "https://x/r.json", null), new FieldMapping(p, "f"));
    List<ForeclosureRecord> r = s.fetch(DataQuery.of("CA", null, null, 10));
    assertEquals(2, r.size());
    assertEquals("A1", r.get(0).caseNumber);
    assertEquals("1 Main St", r.get(0).propertyAddress);
    assertEquals("fc", r.get(0).source);
    assertTrue(seen[0].contains("%24where=") || seen[0].contains("$where="));
  }

  @Test
  public void agentComplaints() throws Exception {
    HttpFetcher http = (url, h) -> "[{\"licensee_name\":\"Jane\",\"fine\":\"500\"}]";
    Properties p = new Properties();
    p.setProperty("a.field.agentName", "licensee_name");
    p.setProperty("a.field.fineAmount", "fine");
    List<AgentComplaintRecord> r = new SocrataAgentComplaintSource("ag",
        new SocrataClient(http, "https://x/r.json", null), new FieldMapping(p, "a"))
        .fetch(DataQuery.of(null, null, null, 10));
    assertEquals("Jane", r.get(0).agentName);
    assertEquals("500", r.get(0).fineAmount);
  }

  @Test
  public void courtListenerAndStore() throws Exception {
    HttpFetcher http = (url, h) -> "{\"next\":null,\"results\":[{\"caseName\":\"Bank v. Doe\","
        + "\"docketNumber\":\"1:26-cv-1\",\"court\":\"Some Court\",\"dateFiled\":\"2026-03-01\","
        + "\"absolute_url\":\"/opinion/1/x/\"}]}";
    List<CourtOrderRecord> r = new CourtListenerOrderSource(http, "https://x/search/", null, "foreclosure")
        .fetch(DataQuery.of(null, null, null, 10));
    assertEquals("Bank v. Doe", r.get(0).caseName);
    assertEquals("https://www.courtlistener.com/opinion/1/x/", r.get(0).url);

    Path dir = Files.createTempDirectory("re");
    Path f = new JsonFileStore(dir).save("court", r);
    JsonObject doc = new JsonParser().parse(Files.readString(f)).getAsJsonObject();
    assertEquals(1, doc.get("count").getAsInt());
    assertEquals("Bank v. Doe", doc.getAsJsonArray("records").get(0).getAsJsonObject().get("caseName").getAsString());
  }
}
