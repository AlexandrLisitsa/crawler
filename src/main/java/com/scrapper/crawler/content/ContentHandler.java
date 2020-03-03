package com.scrapper.crawler.content;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scrapper.crawler.content.TopicUrlContainer.TopicUrl;
import com.scrapper.storage.LocalStorageSaver;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContentHandler {

  @Value("${configFile}")
  private String configFile;
  @Value("${jsContent}")
  private String jsContent;
  @Value("${contentPaginationSelector}")
  private String pagination;
  private TopicUrlContainer urlContainer;
  private Set<Entry<String, JsonElement>> selectors;
  private WebClient client;

  private LocalStorageSaver storageSaver;

  public ContentHandler(LocalStorageSaver storageSaver) {
    this.storageSaver = storageSaver;
  }

  public void setTopicsUrl(Set<String> topicsUrl) {
    urlContainer = new TopicUrlContainer(topicsUrl);
  }

  public void extractContent() {
    while (urlContainer.hasNext()) {
      TopicUrl url = urlContainer.next();
      try {
        initWebClient();
        System.out.println(url);
        List<String> httpMarkup = getHttpMarkups(url.getUrl());
        JsonObject jsonObject = extractContentBySelectors(httpMarkup);
        storageSaver.saveContent(jsonObject);
        System.out.println(jsonObject);
        System.out.println();
        urlContainer.removeTopic(url);
        destroyWebClient();
      } catch (Exception e) {
        destroyWebClient();
        System.err.println("can't load page " + url.getUrl());
      }
    }
  }

  private JsonObject extractContentBySelectors(List<String> markup) {
    JsonObject content = new JsonObject();
    selectors.forEach(selector -> {
      if (selector.getValue().isJsonArray()) {
        JsonArray jsonArray = extractContent(selector.getValue().getAsJsonArray(), markup);
        content.add(selector.getKey(), jsonArray);
      } else {
        Entry contentProperty = extractContentProperties(
            selector, markup);
        content.addProperty(contentProperty.getKey().toString(),
            contentProperty.getValue().toString());
      }
    });

    return content;
  }

  private Map.Entry extractContentProperties(Entry<String, JsonElement> selector,
      List<String> markups) {
    Element element = null;
    for (String markup : markups) {
      Element elem = Jsoup.parse(markup).selectFirst(selector.getValue().getAsString());
      if (element == null) {
        element = elem;
      } else {
        if (!element.text().contains(elem.text())) {
          element.text(element.text() + elem.text());
        }
      }
    }
    if (element == null) {
      return Map.entry(selector.getKey(), "");
    }
    return Map.entry(selector.getKey(), element.text());
  }

  private JsonArray extractContent(JsonArray jsonElements, List<String> markups) {
    JsonObject selectors = jsonElements.get(0).getAsJsonObject();
    Map<String, Elements> elements = new LinkedHashMap<>();
    List<String> keys = selectors.entrySet().stream().map(Entry::getKey)
        .collect(Collectors.toList());

    selectors.entrySet().forEach(selector -> {

      markups.forEach(markup -> {
        Elements select = Jsoup.parse(markup).select(selector.getValue().getAsString());
        if (!select.isEmpty()) {
          Elements existElement = elements.get(selector.getKey());
          if (existElement != null) {
            existElement.addAll(select);
          } else {
            elements.put(selector.getKey(), select);
          }
        }
      });
    });

    JsonArray jsonArray = new JsonArray();

    if (elements.isEmpty()) {
      return jsonArray;
    }

    for (Element element : elements.get(keys.get(0))) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(keys.get(0), element.text());
      jsonArray.add(jsonObject);
    }

    for (int i = 1; i < elements.size(); i++) {
      for (int k = 0; k < jsonArray.size(); k++) {
        JsonObject object = jsonArray.get(k).getAsJsonObject();
        object.addProperty(keys.get(i), elements.get(keys.get(i)).get(k).text());
      }
    }
    return jsonArray;
  }

  private List<String> getHttpMarkups(String url) throws IOException {
    List<String> markups = new ArrayList<>();
    Page page = client.getPage(url);

    if (pagination != null && !pagination.isBlank()) {
      do {
        page = showJsContent(page);
        markups.add(((HtmlPage) page).asXml());
      } while ((page = nextPage(page)) != null);
    } else {
      page = showJsContent(page);
      markups.add(((HtmlPage) page).asXml());
    }

    return markups;
  }

  private Page nextPage(Page page) throws IOException {
    HtmlElement element = ((HtmlPage) page).querySelector(pagination);
    if (element == null) {
      return null;
    } else {
      return element.click();
    }
  }

  private Page showJsContent(Page page) throws IOException {
    if (jsContent != null && !jsContent.isBlank()) {
      HtmlElement elem;
      Page tmp = null;
      while ((elem = (((HtmlPage) page).querySelector(jsContent))) != null) {
        tmp = elem.click();
        if (((HtmlPage) page).asXml().equals(((HtmlPage) tmp).asXml())) {
          break;
        } else {
          page = tmp;
        }
      }
      return tmp;
    }
    return page;
  }

  private void initWebClient() {
    client = new WebClient();
    client.getOptions().setThrowExceptionOnScriptError(false);
    client.getOptions().setJavaScriptEnabled(true);
    client.getOptions().setPopupBlockerEnabled(true);
    client.getOptions().setDownloadImages(false);
    client.getOptions().setTimeout(1000);
  }

  private void destroyWebClient() {
    client.close();
  }

  @PostConstruct
  public void initContentHandler() {
    try {
      selectors = JsonParser.parseReader(
          new FileReader(ContentHandler.class.getClassLoader().getResource(configFile).getFile()))
          .getAsJsonObject().entrySet();
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Selectors config file not found", e);
    }
  }

}
