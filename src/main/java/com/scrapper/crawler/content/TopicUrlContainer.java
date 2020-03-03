package com.scrapper.crawler.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TopicUrlContainer {

  @Value("${retryAvailable}")
  private int retryAvailable;
  private List<TopicUrl> topics = new ArrayList<>();

  void setTopics(Set<String> urls) {
    topics = urls.stream().map(url -> new TopicUrl(url, retryAvailable))
        .collect(Collectors.toList());
  }

  boolean hasNext() {
    while (!topics.isEmpty()) {
      TopicUrl topicUrl = topics.get(0);
      if (topicUrl.getRetryAvailable() != 0) {
        return true;
      } else {
        topics.remove(topicUrl);
      }
    }
    return false;
  }

  TopicUrl next() {
    TopicUrl topicUrl = topics.get(0);
    topicUrl.retryAvailable--;
    return topicUrl;
  }

  void removeTopic(TopicUrl topicUrl) {
    topics.remove(topicUrl);
  }

  @Data
  @AllArgsConstructor
  class TopicUrl {
    private String url;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private int retryAvailable;
  }

}
