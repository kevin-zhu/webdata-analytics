package com.kzhu.realestate.source;

import com.kzhu.realestate.model.ForeclosureRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Foreclosure data from any Socrata dataset, columns mapped via {@link FieldMapping}. */
public class SocrataForeclosureSource implements ForeclosureDataSource {
  private final String name;
  private final SocrataClient client;
  private final FieldMapping map;

  public SocrataForeclosureSource(String name, SocrataClient client, FieldMapping map) {
    this.name = name;
    this.client = client;
    this.map = map;
  }

  @Override public String name() { return name; }

  @Override
  public List<ForeclosureRecord> fetch(DataQuery q) throws IOException {
    List<ForeclosureRecord> out = new ArrayList<>();
    for (Map<String, String> row : client.rows(SocrataQueries.where(map, q), q.keyword, q.limit)) {
      ForeclosureRecord r = new ForeclosureRecord();
      r.caseNumber = map.get(row, "caseNumber");
      r.propertyAddress = map.get(row, "propertyAddress");
      r.city = map.get(row, "city");
      r.state = map.get(row, "state");
      r.zip = map.get(row, "zip");
      r.county = map.get(row, "county");
      r.filingType = map.get(row, "filingType");
      r.status = map.get(row, "status");
      r.filingDate = map.get(row, "filingDate");
      r.auctionDate = map.get(row, "auctionDate");
      r.lender = map.get(row, "lender");
      r.borrower = map.get(row, "borrower");
      r.amountOwed = map.get(row, "amountOwed");
      r.source = name;
      r.raw = row;
      out.add(r);
    }
    return out;
  }
}
