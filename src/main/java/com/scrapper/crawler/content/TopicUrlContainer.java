package com.scrapper.crawler.content;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

class TopicUrlContainer {


  private List<TopicUrl> topics;

  TopicUrlContainer(Set<String> urls) {
    topics = urls.stream().map(TopicUrl::new).collect(Collectors.toList());
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
  @RequiredArgsConstructor
  class TopicUrl {

    @NonNull
    private String url;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.NONE)
    @Value("${retryAvailable}")
    private int retryAvailable;
  }

}
