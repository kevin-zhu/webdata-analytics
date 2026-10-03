package com.kzhu.realestate.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

/** Saves record lists as {@code <dir>/<name>.json}, wrapped in a small metadata envelope. */
public class JsonFileStore {
  private final Path dir;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public JsonFileStore(Path dir) {
    this.dir = dir;
  }

  public <T> Path save(String name, List<T> records) throws IOException {
    Files.createDirectories(dir);
    JsonObject doc = new JsonObject();
    doc.addProperty("source", name);
    doc.addProperty("fetchedAt", Instant.now().toString());
    doc.addProperty("count", records.size());
    doc.add("records", gson.toJsonTree(records));

    Path target = dir.resolve(name + ".json");
    Path tmp = Files.createTempFile(dir, name, ".tmp");
    try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
      gson.toJson(doc, w);
    }
    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }
}
