通过Redis初始化index并自定义datacenterId和workerId,解决 mybatis-plus 雪花算法id冲突问题解决

## 问题描述

k8s里起了多个pod，多个pod有可能部署在不同的机房节点中，理论上机房号和节点号均不能重复

但是发现mybatis-plus的雪花算法不同pod之前生成了相同的id

## 原因分析

1. mybatis-plus默认id生成器生成datacenterId时是读取的机器网卡mac地址后两个字节，生成一个0~31的数字，这里有很大机率生成相同的datacenterId值

```java
private final long datacenterIdBits = 5L;
//-1在二进制中表示为所有位都设置为1（例如，在8位系统中为11111111），
//-1左移5位代表低5位为0
//再与-1异或XOR，最终结果是低5位为1，即31
private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);//31

    /**
     * 数据标识id部分
     */
    protected long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            if (null == this.inetAddress) {
                this.inetAddress = InetAddress.getLocalHost();
            }
            NetworkInterface network = NetworkInterface.getByInetAddress(this.inetAddress);
            if (null == network) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    id = id % (maxDatacenterId + 1);
                }
            }
        } catch (Exception e) {
            logger.warn(" getDatacenterId: " + e.getMessage());
        }
        return id;
    }
```

分析日志：datacenterId

```shell
ee:dc:39:3f:1a:53
531a >> 6 = 0101001100
0101001100 % 32 = 01100

86:65:49:0a:d1:f4
f4d1 >> 6 = 1111010011
1111010011 % 32 = 10011

e2:71:3a:cc:31:c3
c331 >> 6 = 1100001100
1100001100 % 32 = 01100
```

2. mybatis-plus默认id生成器生成workerId时是读取的jvm pid，但是k8s里pod的jvm pid不作处理时默认都是1，最终导致datacenterId + workerId有很大机率不同的pod拥有相同的值

```java
    /**
     * 获取 maxWorkerId
     */
    protected long getMaxWorkerId(long datacenterId, long maxWorkerId) {
        StringBuilder mpid = new StringBuilder();
        mpid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotBlank(name)) {
            /*
             * GET jvmPid
             */
            mpid.append(name.split(StringPool.AT)[0]);
        }
        /*
         * MAC + PID 的 hashcode 获取16个低位
         */
        return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
    }
```

## 解决思路

不同的pod之间分配唯一的datacenterId、workerId

- datacenterId机房id  5bit
- workerId节点id   5bit

配置类

```java

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
* <p>
* mybatis-plus 雪花算法生成器配置。解决多个pod情况下生成的 数据中心id+workerId 重复的问题
* </p>
*/
@Slf4j
@Configuration
public class MybatisPlusSnowflakeConfig {

    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${mybatis-plus-ext.snowflake.redis-key}")
    private String redisKey;
 
    @Bean
    public IdentifierGenerator identifierGenerator(){
        //tested
        Long redisIncrementValue = redisTemplate.opsForValue().increment(redisKey, 1L);
        if (redisIncrementValue == null || redisIncrementValue < 0L){
            throw new RuntimeException(String.format("程序启动失败, 使用redis获取到的雪花算法索引值异常, %s", redisIncrementValue));
        }
 
        //tested
        //从redis索引值中取出dataCenterId、workerId
        MutablePair<Long, Long> longLongMutablePair = generateDatacenterIdAndWorkerIdFromIndex(redisIncrementValue);
        Long dataCenterId = longLongMutablePair.getLeft();
        Long workerId = longLongMutablePair.getRight();
 
        //tested
        //再次检查dataCenterId、workerId值
        if (dataCenterId == null || dataCenterId < 0L || dataCenterId > 31L
                || workerId == null || workerId < 0L || workerId > 31L
        ){
            throw new RuntimeException(String.format("程序启动失败, 使用雪花算法索引值算出的dataCenterId,workerId异常, %s, %s, %s", redisIncrementValue, dataCenterId, workerId));
        }
 
        log.info("mybatis-plus雪花算法datacenterId,workerId生成, redisIncrementValue: {}, dataCenterId: {}, workerId: {}", redisIncrementValue, dataCenterId, workerId);
 
        return new DefaultIdentifierGenerator(workerId, dataCenterId);
    }
 
    /**
     * 从index值中生成雪花算法的datacenterId(5bit)、workerId(5bit)
     *
     * @param index 索引值，如：1。必须为大于等于0的值
     * @return 返回生成好的用MutablePair包装的datacenterId(left)、workerId(right)，其中datacenterId值范围为0~31，workerId值范围也为0~31，如：{0,0}、{1、1}、{0,1}、{0,31}、{31,31}
     * @throws IllegalArgumentException 当参数索引值小于0时会抛出异常
     */
    public static MutablePair<Long, Long> generateDatacenterIdAndWorkerIdFromIndex(long index) {
        if (index < 0) {
            throw new IllegalArgumentException("索引值不能为负数");
        }
 
        long indexBucket = index % 1024L;
        long dataCenterId = indexBucket / 32L;
        long workerId = indexBucket % 32L;
 
        return MutablePair.of(dataCenterId, workerId);
    }

}
```