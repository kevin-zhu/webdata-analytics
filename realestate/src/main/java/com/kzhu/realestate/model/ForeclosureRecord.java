package com.kzhu.realestate.model;

import java.util.Map;

/** A foreclosure filing / notice for a property. */
public class ForeclosureRecord {
  public String caseNumber;
  public String propertyAddress;
  public String city;
  public String state;
  public String zip;
  public String county;
  public String filingType;      // e.g. notice of default, lis pendens, auction notice
  public String status;
  public String filingDate;
  public String auctionDate;
  public String lender;
  public String borrower;
  public String amountOwed;
  public String source;          // name of the data source that produced the record
  public Map<String, String> raw; // untouched source fields
}
