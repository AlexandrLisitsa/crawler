package com.scrapper.storage;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageSaver {

  @Value("${storagePath}")
  private String path;
  private int count=0;

  public void saveContent(JsonObject content) {
    try {
      FileWriter fileWriter = new FileWriter(path + File.separator + count + ".json");
      fileWriter.write(content.toString());
      fileWriter.flush();
      fileWriter.close();
      count++;
    } catch (
        IOException e) {
      e.printStackTrace();
    }
  }

}
