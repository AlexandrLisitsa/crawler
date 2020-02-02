package com.scrapper;

import com.google.gson.JsonObject;
import com.scrapper.crawler.content.ContentHandler;
import com.scrapper.crawler.topic.TopicCrawler;
import com.scrapper.storage.LocalStorageSaver;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Paukan {

  private final TopicCrawler topicCrawler;
  private final ContentHandler contentHandler;
  private final LocalStorageSaver localStorageSaver;

  public Paukan(TopicCrawler topicCrawler, ContentHandler contentHandler, LocalStorageSaver localStorageSaver) {
    this.topicCrawler = topicCrawler;
    this.contentHandler = contentHandler;
    this.localStorageSaver = localStorageSaver;
  }

  @PostConstruct
  public void extract() {
    topicCrawler.start(topicCrawler);
    contentHandler.setTopicsUrl(topicCrawler.getUniqueTopics());
    List<JsonObject> content = contentHandler.extractContent();
    localStorageSaver.saveContent(content);
    System.exit(0);
  }

}
