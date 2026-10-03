# realestate

Java module that pulls real estate related public data and saves it as local JSON files:

| Type | Interface | Implementation | Output |
|---|---|---|---|
| Foreclosures | `ForeclosureDataSource` | `SocrataForeclosureSource` | `foreclosure-<id>.json` |
| Agent/broker complaints & citations | `AgentComplaintDataSource` | `SocrataAgentComplaintSource` | `agent-complaints-<id>.json` |
| Court orders | `CourtOrderDataSource` | `CourtListenerOrderSource` | `courtlistener.json` |

All sources implement `RealEstateDataSource<T>` (`name()`, `fetch(DataQuery)`). To add a new provider,
implement one of the three interfaces and register it in `SourceRegistry`.

Foreclosure and agent-discipline data are published by many states/counties as Socrata open-data
datasets, so those sources are generic: you supply the dataset endpoint and a column mapping in a
properties file (`conf/realestate.properties.sample`). No dataset endpoints are hard-coded — the sample
uses placeholder URLs you must replace with real ones. Court orders come from the CourtListener search API.

## Output format

```json
{ "source": "foreclosure-example", "fetchedAt": "2026-10-02T...Z", "count": 2, "records": [ { ... } ] }
```

Each record carries the normalized fields plus a `raw` map with the untouched source columns.

## Run

First copy the sample config and edit it (relative paths are resolved from the repository root):

```
cp realestate/conf/realestate.properties.sample realestate/conf/realestate.properties
./gradlew :realestate:run --args="--config realestate/conf/realestate.properties --out out --state CA --since 2026-01-01 --limit 500"
```

Options: `--type foreclosure,agent,court`, `--keyword`, `--state`, `--since`, `--limit`.
