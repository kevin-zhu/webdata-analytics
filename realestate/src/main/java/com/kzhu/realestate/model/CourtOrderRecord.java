package com.kzhu.realestate.model;

import java.util.Map;

/** A court order / docket entry related to real estate (foreclosure judgments, injunctions, ...). */
public class CourtOrderRecord {
  public String caseNumber;
  public String caseName;
  public String court;
  public String orderType;
  public String orderDate;
  public String judge;
  public String parties;
  public String summary;
  public String url;
  public String source;
  public Map<String, String> raw;
}
