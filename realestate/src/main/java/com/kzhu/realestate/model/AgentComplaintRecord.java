package com.kzhu.realestate.model;

import java.util.Map;

/** A complaint, citation or disciplinary action against a real estate broker/agent. */
public class AgentComplaintRecord {
  public String recordType;      // COMPLAINT or CITATION (or free text from the source)
  public String caseNumber;
  public String licenseNumber;
  public String agentName;
  public String brokerage;
  public String state;
  public String complaintDate;
  public String violation;
  public String disposition;     // e.g. fine, suspension, dismissed
  public String fineAmount;
  public String description;
  public String source;
  public Map<String, String> raw;
}
