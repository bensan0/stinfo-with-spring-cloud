package com.personal.project.scraperservice.scraper.webmagic;

import org.apache.commons.lang3.ClassPathUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
class WebDriverPool {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static int DEFAULT_CAPACITY = 5;

    private final int capacity;

    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /*
     * new fields for configuring phantomJS
     */
    private WebDriver mDriver = null;
    private boolean mAutoQuitDriver = true;

    private static final String DEFAULT_CONFIG_FILE = ClassPathUtils.class.getClassLoader().getResource("").getFile() + "selenium.properties";
    private static final String DRIVER_FIREFOX = "firefox";
    private static final String DRIVER_CHROME = "chrome";

    protected static Properties sConfig;

    /**
     * Configure the GhostDriver, and initialize a WebDriver instance. This part
     * of code comes from GhostDriver.
     * https://github.com/detro/ghostdriver/tree/master/test/java/src/test/java/ghostdriver
     *
     * @throws IOException
     * @author bob.li.0718@gmail.com
     */
    public void configure() throws IOException {
        String classpath = ClassPathUtils.class.getClassLoader().getResource("").getFile();

        // Read config file
        sConfig = new Properties();
        String configFile = DEFAULT_CONFIG_FILE;
//        if (System.getProperty("selenuim_config") != null) {
//            configFile = System.getProperty("selenuim_config");
//        }
        sConfig.load(new FileReader(configFile));

        String driver = sConfig.getProperty("driver", DRIVER_CHROME);
        // Disable "web-security", enable all possible "ssl-protocols" and
        // "ignore-ssl-errors" for PhantomJSDriver
        // sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new
        // String[] {
        // "--web-security=false",
        // "--ssl-protocol=any",
        // "--ignore-ssl-errors=true"
        // });

        // Start appropriate Driver
        if (driver.equals(DRIVER_FIREFOX)) {
            FirefoxOptions fOps = new FirefoxOptions();
            mDriver = new FirefoxDriver(fOps);
        } else if (driver.equals(DRIVER_CHROME)) {
            /*
            --disable-infobars：禁止显示策略化信息条。
            --no-sandbox：解决DevToolsActivePort文件不存在的报错问题。
            --window-size=1920x3000：设置浏览器分辨率为1920x3000。
            --disable-gpu：禁用GPU加速。
            --incognito：开启隐身模式（无痕模式）。
            --disable-javascript：禁用JavaScript。
            --start-maximized：浏览器最大化运行（全屏窗口）。
            --hide-scrollbars：隐藏滚动条，应对一些特殊页面。
            blink-settings=imagesEnabled=false：不加载图片，提升速度。
            --headless：浏览器不提供可视化页面（无头模式）。Linux下如果系统不支持可视化不加这条会启动失败。
            lang=en_US：设置语言为英文。
            */
            ChromeOptions cOps = new ChromeOptions();
            cOps.addArguments("--disable-infobars");
            cOps.addArguments("--incognito");
            cOps.addArguments("--no-sandbox");
            cOps.addArguments("--disable-gpu");
            cOps.addArguments("blink-settings=imagesEnabled=false");
            cOps.addArguments("--headless");
            cOps.setBinary(classpath + "chrome-headless-shell-mac-arm64/chrome-headless-shell");
            mDriver = new ChromeDriver(cOps);
        }
    }

    /**
     * check whether input is a valid URL
     *
     * @param urlString urlString
     * @return true means yes, otherwise no.
     * @author bob.li.0718@gmail.com
     */
    private boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections
            .synchronizedList(new ArrayList<>());

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();

    public WebDriverPool(int capacity) {
        this.capacity = capacity;
    }

    public WebDriverPool() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * @return
     * @throws InterruptedException
     */
    public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {
                    // add new WebDriver instance into pool
                    try {
                        configure();
                        innerQueue.add(mDriver);
                        webDriverList.add(mDriver);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        checkRunning();
        innerQueue.add(webDriver);
    }

    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }

    public void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (WebDriver webDriver : webDriverList) {
            logger.info("Quit webDriver" + webDriver);
            webDriver.quit();
            webDriver = null;
        }
    }

}
