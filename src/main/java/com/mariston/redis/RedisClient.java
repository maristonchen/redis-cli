package com.mariston.redis;

import com.alibaba.fastjson.JSON;
import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisFuture;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.lettuce.DefaultLettucePool;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * redis client
 *
 * @author mariston
 * @version V1.0
 * @since 2017/09/21
 */
public final class RedisClient implements InitializingBean, DisposableBean {

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(RedisClient.class);

    /**
     * 连接池
     */
    private DefaultLettucePool defaultLettucePool;

    /**
     * 默认数据库索引 为 15
     */
    private final int DEFAULT_DB_INDEX = 15;

    /**
     * default the sum of databases is 16
     */
    private int databases = 16;

    /**
     * 默认字符集
     */
    private final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 永久保存键值对
     *
     * @param key   键
     * @param value 值
     */
    public void put(String key, String value) {
        put(key, value, DEFAULT_DB_INDEX);
    }

    /**
     * 永久保存键值对
     *
     * @param key   键
     * @param value 值
     * @param index 数据库
     */
    public void put(String key, String value, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.set(key.getBytes(Charset.forName(DEFAULT_CHARSET)), value.getBytes(Charset.forName(DEFAULT_CHARSET)));
        } catch (Exception e) {
            logger.error("===永久保存键值对异常：{}", e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 限时保存键值对
     *
     * @param key     键
     * @param seconds 时长
     * @param value   值
     */
    public void put(String key, long seconds, String value) {
        put(key, seconds, value, DEFAULT_DB_INDEX);
    }

    /**
     * 限时保存键值对
     *
     * @param key     键
     * @param value   值
     * @param seconds 时长
     * @param index   数据库
     */
    public void put(String key, long seconds, String value, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.setex(key.getBytes(Charset.forName(DEFAULT_CHARSET)), seconds, value.getBytes(Charset.forName(DEFAULT_CHARSET)));
        } catch (Exception e) {
            logger.error("====限时保存键值对异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 限时保存键值对,值是对象
     *
     * @param key     键
     * @param value   值
     * @param seconds 时长
     * @param index   数据库
     */
    public void putObject(String key, long seconds, Object value, int index) {
        Assert.hasText(key, "key is empty");
        Assert.notNull(value, "value is null ");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.setex(key.getBytes(Charset.forName(DEFAULT_CHARSET)), seconds, ByteUtils.objectToByteArray(value));
        } catch (Exception e) {
            logger.error("====限时保存键值对异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }


    /**
     * 永久保存键值map
     *
     * @param key   键
     * @param map   map<String,String>
     * @param index 数据库
     */
    public void putMap(String key, Map<String, String> map, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            Map<byte[], byte[]> value = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                value.put(entry.getKey().getBytes(Charset.forName(DEFAULT_CHARSET)), entry.getValue().getBytes(Charset.forName(DEFAULT_CHARSET)));
            }
            connection = getConn(index);
            connection.hmset(key.getBytes(Charset.forName(DEFAULT_CHARSET)), value);
        } catch (Exception e) {
            logger.error("====永久保存键值map异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 永久保存file
     *
     * @param key   键
     * @param file  文件
     * @param index 数据库
     */
    public void putFile(String key, File file, int index) {
        Assert.hasText(key, "key is empty");
        Assert.notNull(file, "file is null");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.set(key.getBytes(Charset.forName(DEFAULT_CHARSET)), FileUtils.readFileToByteArray(file));
        } catch (Exception e) {
            logger.error("====永久保存file异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * save the file ,and it's life time is the value of {@code seconds}
     *
     * @param key     key
     * @param seconds the time of expire
     * @param file    file
     * @param index   the index of database
     */
    public void putFile(String key, File file, long seconds, int index) {
        Assert.hasText(key, "key is null or empty");
        Assert.notNull(file, "file is null");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.setex(key.getBytes(Charset.forName(DEFAULT_CHARSET)), seconds, FileUtils.readFileToByteArray(file));
        } catch (Exception e) {
            logger.error("====保存file异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * save the file ,and it's life time is forever
     *
     * @param key      key
     * @param filePath path of the  file
     * @param index    the index of database
     */
    public void putFile(String key, String filePath, int index) {
        Assert.hasText(key, "filePath is null or empty");
        this.putFile(key, new File(filePath), index);
    }

    /**
     * save the file ,and it's life time is the value of {@code seconds}
     *
     * @param key      key
     * @param seconds  the time of expire
     * @param filePath path of the  file
     * @param index    the index of database
     */
    public void putFile(String key, String filePath, long seconds, int index) {
        Assert.hasText(key, "filePath is null or empty");
        this.putFile(key, new File(filePath), seconds, index);
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return String
     */
    public String get(String key) {
        return get(key, DEFAULT_DB_INDEX);
    }

    /**
     * 获取值
     *
     * @param key   键
     * @param index 数据库 索引
     * @return String
     */
    public String get(String key, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        String value = StringUtils.EMPTY;
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            RedisFuture<byte[]> bytes = connection.get(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
            value = new String(bytes.get(), Charset.forName(DEFAULT_CHARSET));
        } catch (Exception e) {
            logger.error("===从缓存中获取值异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return value;
    }

    /**
     * 获取值
     *
     * @param key   键
     * @param index 数据库 索引
     * @return String
     */
    public <T> T get(String key, int index, Class<T> clazz) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        Assert.notNull(clazz, "the class of object is null");
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            RedisFuture<byte[]> bytes = connection.get(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
            String value = new String(bytes.get(), Charset.forName(DEFAULT_CHARSET));
            if (StringUtils.isNotBlank(value)) {
                return JSON.parseObject(value, clazz);
            }
        } catch (Exception e) {
            logger.error("===从缓存中获取值异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return null;
    }

    /**
     * 获取值
     *
     * @param key   键
     * @param index 数据库 索引
     * @return String
     */
    public <T> T getObject(String key, int index, Class<T> clazz) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        Assert.notNull(clazz, "the class of object is null");
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            RedisFuture<byte[]> bytes = connection.get(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
            Object obj = ByteUtils.byteArrayToObject(bytes.get());
            if (obj != null && clazz.equals(obj.getClass())) {
                return clazz.cast(obj);
            }
        } catch (Exception e) {
            logger.error("===从缓存中获取值异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return null;
    }

    /**
     * 获取键值map
     *
     * @param key   键
     * @param index 数据库 索引
     * @return String
     */
    public Map<String, String> getMap(String key, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        Map<String, String> map = new HashMap<>();
        try {
            connection = getConn(index);
            RedisFuture<Map<byte[], byte[]>> bytes = connection.hgetall(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
            Map<byte[], byte[]> bm = bytes.get();
            if (!ObjectUtils.isEmpty(bm)) {
                for (Map.Entry<byte[], byte[]> entry : bm.entrySet()) {
                    String cKey = new String(entry.getKey(), Charset.forName(DEFAULT_CHARSET));
                    String cValue = new String(entry.getValue(), Charset.forName(DEFAULT_CHARSET));
                    map.put(cKey, cValue);
                }
            }
        } catch (Exception e) {
            logger.error("===获取键值map异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return map;
    }

    /**
     * 获取键值map中field
     *
     * @param key   键
     * @param index 数据库 索引
     * @return String
     */
    public String getField(String key, String field, int index) {
        Assert.hasText(key, "key is null or empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        Assert.hasText(field, "field is null or empty");
        RedisAsyncConnection<byte[], byte[]> connection = null;
        String value = StringUtils.EMPTY;
        try {
            connection = getConn(index);
            RedisFuture<byte[]> bytes = connection.hget(key.getBytes(Charset.forName(DEFAULT_CHARSET)), field.getBytes(Charset.forName(DEFAULT_CHARSET)));
            value = new String(bytes.get(), Charset.forName(DEFAULT_CHARSET));
        } catch (Exception e) {
            logger.error("===获取键值map中field异常[{}]{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return value;
    }

    /**
     * to get the file from the redis database by key
     *
     * @param key      key
     * @param filePath the directory of file
     * @param index    the index of database
     * @return {@link File}
     */
    public File getFile(String key, String filePath, int index) {
        Assert.hasText(key, "key is null or empty");
        Assert.hasText(filePath, "the directory of file is null or empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        File file = null;
        try {
            connection = getConn(index);
            RedisFuture<byte[]> bytes = connection.get(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
            file = new File(filePath);
            FileUtils.writeByteArrayToFile(file, bytes.get());
        } catch (Exception e) {
            logger.error("===获取file异常[{}]{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return file;
    }

    /**
     * 删除键值对
     *
     * @param key 键
     */
    public void delete(String key) {
        delete(key, DEFAULT_DB_INDEX);
    }

    /**
     * 删除键值对
     *
     * @param key   键
     * @param index 数据库
     */
    public void delete(String key, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.del(key.getBytes(Charset.forName(DEFAULT_CHARSET)));
        } catch (Exception e) {
            logger.error("===删除键值对异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 删除键值map中的field
     *
     * @param key   键
     * @param index 数据库
     */
    public void delField(String key, int index, String... fields) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        Assert.notEmpty(fields, "fields is null or the size is zero");
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            byte[][] bytes = new byte[0][];
            for (String field : fields) {
                bytes = ArrayUtils.add(bytes, field.getBytes(Charset.forName(DEFAULT_CHARSET)));
            }
            connection.hdel(key.getBytes(Charset.forName(DEFAULT_CHARSET)), bytes);
        } catch (Exception e) {
            logger.error("===删除键值map中的field异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 清空数据库
     *
     * @param index 数据库
     */
    public void flushdb(int index) {
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            connection.flushdb();
        } catch (Exception e) {
            logger.error("===清空数据库异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
    }

    /**
     * 设置超时时间
     *
     * @param key     键
     * @param seconds 时长
     * @return boolean
     */
    public boolean expire(String key, long seconds) {
        return expire(key, seconds, DEFAULT_DB_INDEX);
    }

    /**
     * 设置超时时间
     *
     * @param key     键
     * @param seconds 时长
     * @param index   索引
     * @return boolean
     */
    public boolean expire(String key, long seconds, int index) {
        Assert.hasText(key, "key is empty");
        Assert.isTrue(index >= 0 && index < databases, "the index of database range must be between 0 and " + databases);
        RedisAsyncConnection<byte[], byte[]> connection = null;
        try {
            connection = getConn(index);
            RedisFuture<Boolean> bool = connection.expire(key.getBytes(Charset.forName(DEFAULT_CHARSET)), seconds);
            return bool.get();
        } catch (Exception e) {
            logger.error("===设置超时时间异常[{}]:{}", e.getStackTrace()[0], e.getMessage());
        } finally {
            if (connection != null) {
                defaultLettucePool.returnResource(connection);
            }
        }
        return false;
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        if (defaultLettucePool != null) {
            defaultLettucePool.destroy();
        }
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(defaultLettucePool, "the default lettuce pool is null ");
    }

    /**
     * 获取连接
     *
     * @param index 数据库索引
     * @return {@link RedisAsyncConnection}
     */
    private RedisAsyncConnection<byte[], byte[]> getConn(int index) {
        RedisAsyncConnection<byte[], byte[]> connection = defaultLettucePool.getResource();
        connection.select(index);
        return connection;
    }

    /**
     * 设置连接池
     *
     * @param defaultLettucePool 连接池
     */
    public void setDefaultLettucePool(DefaultLettucePool defaultLettucePool) {
        this.defaultLettucePool = defaultLettucePool;
    }

    /**
     * set the sum of databases
     *
     * @param databases int
     */
    public void setDatabases(int databases) {
        this.databases = databases;
    }
}
