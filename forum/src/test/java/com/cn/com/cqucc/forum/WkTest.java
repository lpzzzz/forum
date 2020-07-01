package com.cn.com.cqucc.forum;

import java.io.IOException;

public class WkTest {
    public static void main(String[] args) {
        String cmd = "e:\\develop\\wkhtmltopdf\\install\\bin\\wkhtmltoimage  --quality 75 http://47.93.51.201:9999/forum/index e:\\Develop\\wkhtmltopdf\\save\\wk-images\\888.png";
        try {
            Runtime.getRuntime().exec(cmd); // 他们之间的执行是异步的 并行执行 可能已经打印完成之后还未完成图片的生成。
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
