package com.mariston.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.HashMap;
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