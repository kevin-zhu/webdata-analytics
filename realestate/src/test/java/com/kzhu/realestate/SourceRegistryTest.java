package com.kzhu.realestate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kzhu.realestate.source.SourceRegistry;
import java.util.Properties;
import org.junit.Test;

public class SourceRegistryTest {
  private static Properties props() {
    Properties p = new Properties();
    p.setProperty("foreclosure.CA.endpoint", "https://ca/r.json");
    p.setProperty("foreclosure.TX.endpoint", "https://tx/r.json");
    p.setProperty("agent.TX.endpoint", "https://tx/a.json");
    return p;
  }

  @Test
  public void stateSelectsOnlyThatStatesEndpoint() {
    SourceRegistry r = new SourceRegistry(props(), null, "tx");
    assertEquals(1, r.foreclosureSources().size());
    assertEquals("foreclosure-TX", r.foreclosureSources().get(0).name());
    assertEquals("agent-complaints-TX", r.agentComplaintSources().get(0).name());
    assertTrue(r.hasState("foreclosure"));
  }

  @Test
  public void unknownStateYieldsNothing() {
    SourceRegistry r = new SourceRegistry(props(), null, "NY");
    assertTrue(r.foreclosureSources().isEmpty());
    assertFalse(r.hasState("agent"));
  }

  @Test
  public void noStateUsesAllConfiguredStates() {
    SourceRegistry r = new SourceRegistry(props(), null);
    assertEquals(2, r.foreclosureSources().size());
    assertEquals(1, r.agentComplaintSources().size());
  }
}
