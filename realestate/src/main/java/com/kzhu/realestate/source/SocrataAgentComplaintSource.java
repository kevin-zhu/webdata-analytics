package com.kzhu.realestate.source;

import com.kzhu.realestate.model.AgentComplaintRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Agent/broker complaints and citations from a Socrata dataset (e.g. a state real estate commission). */
public class SocrataAgentComplaintSource implements AgentComplaintDataSource {
  private final String name;
  private final SocrataClient client;
  private final FieldMapping map;

  public SocrataAgentComplaintSource(String name, SocrataClient client, FieldMapping map) {
    this.name = name;
    this.client = client;
    this.map = map;
  }

  @Override public String name() { return name; }

  @Override
  public List<AgentComplaintRecord> fetch(DataQuery q) throws IOException {
    List<AgentComplaintRecord> out = new ArrayList<>();
    for (Map<String, String> row : client.rows(SocrataQueries.where(map, q), q.keyword, q.limit)) {
      AgentComplaintRecord r = new AgentComplaintRecord();
      r.recordType = map.get(row, "recordType");
      r.caseNumber = map.get(row, "caseNumber");
      r.licenseNumber = map.get(row, "licenseNumber");
      r.agentName = map.get(row, "agentName");
      r.brokerage = map.get(row, "brokerage");
      r.state = map.get(row, "state");
      r.complaintDate = map.get(row, "complaintDate");
      r.violation = map.get(row, "violation");
      r.disposition = map.get(row, "disposition");
      r.fineAmount = map.get(row, "fineAmount");
      r.description = map.get(row, "description");
      r.source = name;
      r.raw = row;
      out.add(r);
    }
    return out;
  }
}
