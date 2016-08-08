package com.ws.ex.crawl;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;
import com.mongodb.util.JSON;
import com.ws.ex.crawl.po.ZCFGModel;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 爬取http://www.tianyisw.com/ 政策法规数据
 * @author welcome
 *
 */
public class CrawlZCFG implements PageProcessor {

    private Site site = Site.me()/*.setDomain("www.tianyisw.com")*/
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");

    // 用于匹配网页正文中分页链接
    private static final String url = "http://www\\.tianyisw\\.com/Policies\\.asp\\?owen1=政策法规&owen2=&page=\\d+";

    // 列表页请求地址
    private static final String URL_LIST = "http://www\\.tianyisw.com/Policies\\.asp\\?owen1=%D5%FE%B2%DF%B7%A8%B9%E6&owen2=&page=\\d+";

    // 详情页请求地址
    private static final String URL_POST = "http://www\\.tianyisw\\.com/show_Policies\\.asp\\?id=\\d+";

    private MongoClient mongoClient = new MongoClient();
    private DB db = mongoClient.getDB("mymongo");
    private DBCollection collection = db.getCollection("test_tianyisw");

    @Override
    public void process(Page page) {

        // 若请求地址为列表页
        if(page.getUrl().regex(URL_LIST).match()){

            // 添加详情地址到爬虫请求列表中
            page.addTargetRequests(page.getHtml().links().regex(URL_POST).all());

            // 处理匹配到的分页请求链接
            List<String> list = page.getHtml().links().regex(url).all();

            ArrayList<String> lst = new ArrayList<String>();
            for (String string : list) {
                // 处理地址中的中文
                String url = string.replace("政策法规", "%D5%FE%B2%DF%B7%A8%B9%E6");
                lst.add(url);
            }
            // 将分页请求链接地址添加到爬虫请求列表中
            page.addTargetRequests(lst);
        }else{
            ZCFGModel zcfgModel = new ZCFGModel();
            // 详情页数据解析 为详情地址所指向的内容页
            page.putField("title", page.getHtml().xpath("//table/tbody/tr/td[@class=\"tit\"]/strong/text()").toString());
           // page.putField("body", page.getHtml().xpath("//td[@class=\"hanggao\"]").toString());
            zcfgModel.setTitle(page.getHtml().xpath("//table/tbody/tr/td[@class=\"tit\"]/strong/text()").toString());
            DBObject object = (BasicDBObject) JSON.parse(zcfgModel.toJson());
            collection.insert(object);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new CrawlZCFG())
                // 爬虫初始地址
                .addUrl("http://www.tianyisw.com/Policies.asp?owen1=%D5%FE%B2%DF%B7%A8%B9%E6&owen2=&page=1")
                .thread(6)
                .run();
    }
}