package com.personal.project.scraperservice.scraper.webmagic;

import com.personal.project.scraperservice.config.BrowserConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * 使用Selenium调用浏览器进行渲染。目前仅支持chrome。<br>
 * 需要下载Selenium driver支持。<br>
 *
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:37 <br>
 */
@Slf4j
public class SeleniumDownloader extends AbstractDownloader implements Closeable {

	private volatile WebDriverPool webDriverPool;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private int sleepTime = 0;

	private int poolSize = 1;

	private Duration waitTime = null;

	private ExpectedCondition waitCondition = null;

	private BrowserConfig browserConfig;

	private boolean needScrollPage;

	@Getter
	private Set<String> failedUrls;

	/**
	 * 新建
	 *
	 * @param chromeDriverPath chromeDriverPath
	 */
	public SeleniumDownloader(String chromeDriverPath) {
		System.getProperties().setProperty("webdriver.chrome.driver",
				chromeDriverPath);
	}

	public SeleniumDownloader(BrowserConfig browserConfig, Duration waitTime, ExpectedCondition waitCondition, Set<String> failedUrls, boolean needScrollPage) {

		this.browserConfig = browserConfig;

		System.getProperties().setProperty("webdriver.chrome.driver",
				browserConfig.getDriverpath());

		this.waitTime = waitTime;

		this.waitCondition = waitCondition;

		this.failedUrls = failedUrls;

		this.needScrollPage = needScrollPage;
	}

	/**
	 * Constructor without any filed. Construct PhantomJS browser
	 *
	 * @author bob.li.0718@gmail.com
	 */
	public SeleniumDownloader() {
	}

	/**
	 * set sleep time to wait until load success
	 *
	 * @param sleepTime sleepTime
	 * @return this
	 */
	public SeleniumDownloader setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
		return this;
	}

	@Override
	public Page download(Request request, Task task) {
		checkInit();
		WebDriver webDriver = null;
		Page page = Page.fail(request);
		try {
			webDriver = webDriverPool.get(browserConfig.getDriver(), browserConfig.getBinarypath());
			logger.info("downloading page " + request.getUrl());
			webDriver.get(request.getUrl());
			try {
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				throw new Exception("爬頁面時執行緒等待錯誤");
			}
			WebDriver.Options manage = webDriver.manage();
			Site site = task.getSite();
			if (site.getCookies() != null) {
				for (Map.Entry<String, String> cookieEntry : site.getCookies()
						.entrySet()) {
					Cookie cookie = new Cookie(cookieEntry.getKey(),
							cookieEntry.getValue());
					manage.addCookie(cookie);
				}
			}

			/*
			 * TODO You can add mouse event or other processes
			 *
			 * @author: bob.li.0718@gmail.com
			 */
			if (waitTime != null && waitCondition != null) {
				new WebDriverWait(webDriver, waitTime).until(waitCondition);
			}

			//防止頁面資料需要捲動才會完整加載
			if (needScrollPage) {
				JavascriptExecutor js = (JavascriptExecutor) webDriver;
				long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
				WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(200));
				while (true) {
					// 捲動到頁面底部
					js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

					// 使用Timer等待頁面加載, 用Thread.sleep有busy-waiting/dead lock風險
					CountDownLatch latch = new CountDownLatch(1);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							latch.countDown();
						}
					}, 3000);

					try {
						latch.await();
					} catch (InterruptedException e) {
						throw new Exception("下拉爬蟲頁面出錯");
					}

					// 等待新元素出現
					wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul li")));

					// 計算新的捲動高度
					long newHeight = (long) js.executeScript("return document.body.scrollHeight");

					// 如果高度沒有變化,表示已經到達底部
					if (newHeight == lastHeight) {
						break;
					}
					lastHeight = newHeight;
				}
			}

			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");
			page.setDownloadSuccess(true);
			page.setRawText(content);
			page.setHtml(new Html(content, request.getUrl()));
			page.setUrl(new PlainText(request.getUrl()));
			page.setRequest(request);
			onSuccess(page, task);
		} catch (Exception e) {
			logger.warn("download page {} error", request.getUrl(), e);
			if (failedUrls != null) {
				failedUrls.add(request.getUrl());
			}
//            onError(page, task, e);
		} finally {
			if (webDriver != null) {
				webDriverPool.returnToPool(webDriver);
			}
		}
		return page;
	}

	private void checkInit() {
		if (webDriverPool == null) {
			synchronized (this) {
				webDriverPool = new WebDriverPool(poolSize);
			}
		}
	}

	@Override
	public void setThread(int thread) {
		this.poolSize = thread;
	}

	@Override
	public void close() throws IOException {
		webDriverPool.closeAll();
	}

	@Override
	protected void onError(Page page, Task task, Throwable e) {
		super.onError(page, task, e);
	}

}
