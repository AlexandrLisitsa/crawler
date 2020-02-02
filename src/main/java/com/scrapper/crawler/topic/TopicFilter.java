package com.scrapper.crawler.topic;

import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopicFilter {

  private final Pattern FILTERS
      = Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");

  @Value("${topicsRootUrl}")
  private String topicsRootUrl;
  @Value("${topicsPaginationPattern}")
  private String paginationPattern;
  @Value("${topicUrlPattern}")
  private String topicPattern;

  private Pattern topic;
  private Pattern pagination;

  public boolean matchRoot(String url) {
    if (FILTERS.matcher(url).matches()) {
      return false;
    } else if (paginationPattern.equals("")) {
      return url.contains(topicsRootUrl);
    } else {
      return pagination.matcher(url).matches();
    }
  }

  public boolean matchTopic(String url) {
    return topic.matcher(url).matches();
  }

  @PostConstruct
  private void init() {
    topic = Pattern.compile(topicPattern);
    pagination = Pattern.compile(paginationPattern);
  }

}
