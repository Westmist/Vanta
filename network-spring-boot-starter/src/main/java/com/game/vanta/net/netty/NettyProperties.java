package com.game.vanta.net.netty;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "network.netty")
public class NettyProperties {

  private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

  private int bossThreads = Math.max(1, AVAILABLE_PROCESSORS / 2);

  private int workerThreads = AVAILABLE_PROCESSORS * 2;

  private long readerIdleTime = 20;

  private long writerIdleTime;

  private long allIdleTime;

  public int getBossThreads() {
    return bossThreads;
  }

  public void setBossThreads(int bossThreads) {
    this.bossThreads = bossThreads;
  }

  public int getWorkerThreads() {
    return workerThreads;
  }

  public void setWorkerThreads(int workerThreads) {
    this.workerThreads = workerThreads;
  }

  public long getReaderIdleTime() {
    return readerIdleTime;
  }

  public void setReaderIdleTime(long readerIdleTime) {
    this.readerIdleTime = readerIdleTime;
  }

  public long getWriterIdleTime() {
    return writerIdleTime;
  }

  public void setWriterIdleTime(long writerIdleTime) {
    this.writerIdleTime = writerIdleTime;
  }

  public long getAllIdleTime() {
    return allIdleTime;
  }

  public void setAllIdleTime(long allIdleTime) {
    this.allIdleTime = allIdleTime;
  }
}
