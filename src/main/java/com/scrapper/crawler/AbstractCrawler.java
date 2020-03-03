package com.scrapper.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.File;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractCrawler extends WebCrawler {


  @Value("${topicsRootUrl}")
  protected String topicsRootUrl;
  @Value("${crawlersCount}")
  private int crawlersCount;
  @Value("${storagePath}")
  private String storagePath = "";
  @Value("${requestDelay}")
  private int requestDelay;

  private CrawlConfig config = new CrawlConfig();
  private PageFetcher pageFetcher = new PageFetcher(config);
  private RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
  private RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
  private CrawlController controller;

  public void start(WebCrawler crawler) {
    config.setPolitenessDelay(requestDelay);
    if (storagePath != null) {
      File crawlStorage = new File(storagePath);
      config.setCrawlStorageFolder(crawlStorage.getAbsolutePath());
    }
    try {
      controller = new CrawlController(config, pageFetcher, robotstxtServer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    controller.addSeed(topicsRootUrl);
    CrawlController.WebCrawlerFactory<WebCrawler> factory = () -> crawler;
    controller.start(factory, crawlersCount);
  }

  protected void shutdown() {
    controller.shutdown();
  }

}
