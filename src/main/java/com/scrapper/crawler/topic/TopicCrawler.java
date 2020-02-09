package com.scrapper.crawler.topic;

import com.scrapper.crawler.AbstractCrawler;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopicCrawler extends AbstractCrawler {

  private Set<String> uniqueTopics = new HashSet<>();

  private final TopicFilter topicFilter;
  @Value("${maxTopicAmount}")
  private int maxTopicAmount;

  public TopicCrawler(TopicFilter topicFilter) {
    this.topicFilter = topicFilter;
  }

  @Override
  public boolean shouldVisit(Page referringPage, WebURL url) {
    return topicFilter.matchRoot(url.getURL().toLowerCase());
  }

  @Override
  public void visit(Page page) {
    if (page.getParseData() instanceof HtmlParseData) {
      HtmlParseData data = (HtmlParseData) page.getParseData();
      Set<WebURL> outgoingUrls = data.getOutgoingUrls();
      outgoingUrls.forEach(x -> {
        if (topicFilter.matchTopic(x.getURL())) {
          addUrl(x.getURL());
        }
      });

      System.out.println(page.getWebURL().getURL() + " Unique Topic:" + uniqueTopics.size());
    }
  }

  private void addUrl(String url) {
    if (maxTopicAmount == -1) {
      uniqueTopics.add(url);
    } else if (uniqueTopics.size() < maxTopicAmount) {
      uniqueTopics.add(url);
    } else {
      this.shutdown();
    }
  }

  public Set<String> getUniqueTopics() {
    return uniqueTopics;
  }
}
