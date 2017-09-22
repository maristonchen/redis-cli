package com.mariston.redis;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * (用一句话描述该文件做什么)
 *
 * @author mariston
 * @version V1.0
 * @since 2017/09/21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-redis.xml"})
public class RedisClientTest {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Resource
    private RedisClient redisClient;

    @Test
    public void put() throws Exception {
        redisClient.put("test","test");
    }

    @Test
    public void putObject() throws Exception {
        Heart heart = new Heart();
        heart.setChannel(1);
        byte[] data = new byte[]{120,23,34,127,22,8,74};
        heart.setData(data);
        heart.setLeadEvent((short) 10);
        heart.setMonitoredTime(23444);
        heart.setSampleRate(39483948);
        heart.setRemark("多少的发撒旦法");
        redisClient.putObject("testObject", 1200, heart, 5);
    }

    @Test
    public void putFile() throws Exception {
        File file = new File("G:/LenovoHdReport.txt");
        redisClient.putFile("LenovoHdReport", file, 100,1);
        redisClient.putFile("LenovoHdReport1", "G:/LenovoHdReport.txt",4);
        redisClient.putFile("LenovoHdReport2", "G:/LenovoHdReport.txt", 100,4);
    }

    @Test
    public void putMap() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("第一", "1");
        map.put("第二", "2");
        map.put("第三", "3");
        redisClient.putMap("map",map,1);
    }

    @Test
    public void get() throws Exception {

        String value = redisClient.get("test");

        logger.info("the vlaue is {}",value);

    }

    @Test
    public void getObject() throws Exception {

        Heart heart = redisClient.getObject("testObject", 5, Heart.class);

        logger.info("the vlaue is {}",heart);

    }

    @Test
    public void getFile() throws Exception {

        File file = redisClient.getFile("LenovoHdReport", "F:/LenovoHdReport.txt", 2);
        List<String> lines = FileUtils.readLines(file, "GBK");
        for (String line : lines) {
            System.out.println(line);
        }

    }

    @Test
    public void getMap() throws Exception {
        Map<String, String> map = redisClient.getMap("map", 1);
        logger.info("the map is {}", map);
    }

    @Test
    public void getField() throws Exception {
        String field = redisClient.getField("map", "第一", 1);
        logger.info("the field is {}",field);
    }

    @Test
    public void delete() throws Exception {
        redisClient.delete("test");
        this.get();
    }

    @Test
    public void delField() throws Exception {
        redisClient.delField("map", 1, "第一", "第二");
        this.getMap();
    }

    @Test
    public void flushdb() throws Exception {
        redisClient.flushdb(0);
    }

    @Test
    public void expire() throws Exception {

        redisClient.put("expire", "expire");
        redisClient.put("expire1",2000, "expire1");
        redisClient.put("expire2", "expire2");
        redisClient.expire("expire", 1000);

    }

}