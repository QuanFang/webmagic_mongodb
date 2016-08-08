package com.ws.ex.crawl.po;

import com.ws.ex.crawl.jutils.JsonUtils;

/**
 * Created by laowang on 16-8-8.
 */
public class ZCFGModel {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;

    public String toJson(){
        return JsonUtils.toJson(this);
    }
}
