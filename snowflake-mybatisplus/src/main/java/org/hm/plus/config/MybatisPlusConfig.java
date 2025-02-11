package org.hm.plus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Description:
 * @Author: lyflexi
 * @project: mybatis-plus-practice
 * @Date: 2024/9/10 20:43
 */
@Configuration
@MapperScan("org.hm.plus.dao")
@Slf4j
public class MybatisPlusConfig {
    /**
     * Mybatis-plus 主键雪花算法构造自增参数
     */
    public static final String CACHE_NAME_MYBATIS = "MYBATIS:snowflake:id-generator-key";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        // 如果配置多个插件, 切记分页最后添加
        return interceptor;
    }

//     @Bean
//     public DefaultIdentifierGenerator defaultIdentifierGenerator () throws UnknownHostException {
    // 容器部署多个节点可能会导致雪花算法ID重复
//         return  new DefaultIdentifierGenerator(InetAddress.getLocalHost());
//     }
    /**
     * <p>
     * mybatis-plus 雪花算法生成器配置。解决多个pod情况下生成的 数据中心id+workerId 重复的问题
     * </p>
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        Long redisIncrementValue = redisTemplate.opsForValue().increment(CACHE_NAME_MYBATIS, 1L);
        if (redisIncrementValue == null || redisIncrementValue < 0L) {
            throw new RuntimeException(String.format("程序启动失败, 使用redis获取到的雪花算法索引值异常, %s", redisIncrementValue));
        }
        log.info("redisIncrementValue: {}", redisIncrementValue);

        // 从redis索引值中取出dataCenterId、workerId
        MutablePair<Long, Long> longLongMutablePair = generateDatacenterIdAndWorkerIdFromIndex(redisIncrementValue);
        Long dataCenterId = longLongMutablePair.getLeft();
        Long workerId = longLongMutablePair.getRight();

        // 再次检查dataCenterId、workerId值
        if (dataCenterId == null || dataCenterId < 0L || dataCenterId > 31L
                || workerId == null || workerId < 0L || workerId > 31L
        ) {
            throw new RuntimeException(String.format("程序启动失败, 使用雪花算法索引值算出的dataCenterId,workerId异常, %s, %s, %s", redisIncrementValue, dataCenterId, workerId));
        }

        log.info("mybatis-plus雪花算法datacenterId,workerId生成, redisIncrementValue: {}, dataCenterId: {}, workerId: {}", redisIncrementValue, dataCenterId, workerId);
        return new DefaultIdentifierGenerator(workerId, dataCenterId);
    }

    /**
     * 从index值中生成雪花算法的datacenterId(5bit)、workerId(5bit)
     *
     * @param index 索引值，如：1。必须为大于等于0的值
     * @return 返回生成好的用MutablePair包装的datacenterId(left)、workerId(right)，其中datacenterId值范围为0~31，workerId值范围也为0~31，
     * 如：{0,0}、{1、1}、{0,1}、{0,31}、{31,31}
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

    public static void main(String[] args) {

    }

}