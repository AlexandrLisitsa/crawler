package com.scrapper;

import com.scrapper.crawler.content.ContentHandler;
import com.scrapper.crawler.topic.TopicCrawler;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Paukan {

  private final TopicCrawler topicCrawler;
  private final ContentHandler contentHandler;

  public Paukan(TopicCrawler topicCrawler, ContentHandler contentHandler) {
    this.topicCrawler = topicCrawler;
    this.contentHandler = contentHandler;
  }

  @PostConstruct
  public void extract() {
    topicCrawler.start(topicCrawler);
    contentHandler.setTopicsUrl(topicCrawler.getUniqueTopics());
    contentHandler.extractContent();
    System.exit(0);
  }

}
