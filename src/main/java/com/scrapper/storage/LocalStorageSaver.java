package com.scrapper.storage;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageSaver {

  @Value("${storagePath}")
  private String path;

  public void saveContent(List<JsonObject> content) {
    try {
      for (int i = 0; i < content.size(); i++) {
        FileWriter fileWriter = new FileWriter(path + File.separator + i + ".json");
        fileWriter.write(content.get(i).toString());
        fileWriter.flush();
        fileWriter.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
