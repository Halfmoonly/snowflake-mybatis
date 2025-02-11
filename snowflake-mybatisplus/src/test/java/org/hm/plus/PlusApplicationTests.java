package org.hm.plus;

import org.apache.commons.lang3.tuple.MutablePair;
import org.hm.plus.config.MybatisPlusConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlusApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void generateDatacenterIdAndWorkerIdFromIndex() {
		//legal
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(0L);
			Assert.assertEquals(0L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(0L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(1L);
			Assert.assertEquals(0L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(1L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(2L);
			Assert.assertEquals(0L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(2L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(31L);
			Assert.assertEquals(0L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(32L);
			Assert.assertEquals(1L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(0L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(33L);
			Assert.assertEquals(1L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(1L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(63L);
			Assert.assertEquals(1L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(64L);
			Assert.assertEquals(2L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(0L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(991L);
			Assert.assertEquals(30L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(992L);
			Assert.assertEquals(31L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(0L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(993L);
			Assert.assertEquals(31L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(1L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(1023L);
			Assert.assertEquals(31L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(1024L);
			Assert.assertEquals(0L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(0L, longLongMutablePair.getRight().longValue());
		}

		//integer.max
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(2147483647L);
			Assert.assertEquals(31L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}
		//long.max
		{
			MutablePair<Long, Long> longLongMutablePair = MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(9223372036854775807L);
			Assert.assertEquals(31L, longLongMutablePair.getLeft().longValue());
			Assert.assertEquals(31L, longLongMutablePair.getRight().longValue());
		}

//		//illegal
//		{
//			Assert.assertEquals("索引值不能为负数", Assert.assertThrows(IllegalArgumentException.class, () -> MybatisPlusConfig.generateDatacenterIdAndWorkerIdFromIndex(-1)).getMessage());
//		}
	}
}
