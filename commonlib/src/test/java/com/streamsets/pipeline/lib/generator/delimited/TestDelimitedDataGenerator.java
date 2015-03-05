/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.lib.generator.delimited;

import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.OnRecordError;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.config.CsvHeader;
import com.streamsets.pipeline.config.CsvMode;
import com.streamsets.pipeline.lib.generator.DataGenerator;
import com.streamsets.pipeline.sdk.ContextInfoCreator;
import com.streamsets.pipeline.sdk.RecordCreator;
import org.apache.commons.csv.CSVFormat;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDelimitedDataGenerator {

  @Test
  public void testFactory() throws Exception {
    Stage.Context context = ContextInfoCreator.createTargetContext("i", false, OnRecordError.TO_ERROR);
    Map<String, Object> configs = new HashMap<>();
    DelimitedCharDataGeneratorFactory.registerConfigs(configs);
    DelimitedCharDataGeneratorFactory factory = new DelimitedCharDataGeneratorFactory(context, CSVFormat.DEFAULT,
                                                                                      CsvHeader.IGNORE_HEADER, configs);
    DelimitedDataGenerator generator = (DelimitedDataGenerator) factory.getGenerator(new StringWriter());
    Assert.assertEquals(CSVFormat.DEFAULT, generator.getFormat());
    Assert.assertEquals(CsvHeader.IGNORE_HEADER, generator.getHeader());
    Assert.assertEquals(CsvHeader.IGNORE_HEADER, generator.getHeader());
    Assert.assertEquals("header", generator.getHeaderKey());
    Assert.assertEquals("value", generator.getValueKey());

    configs.put(DelimitedCharDataGeneratorFactory.HEADER_KEY, "foo");
    configs.put(DelimitedCharDataGeneratorFactory.VALUE_KEY, "bar");
    factory = new DelimitedCharDataGeneratorFactory(context, CSVFormat.DEFAULT, CsvHeader.IGNORE_HEADER, configs);
    generator = (DelimitedDataGenerator) factory.getGenerator(new StringWriter());
    Assert.assertEquals("foo", generator.getHeaderKey());
    Assert.assertEquals("bar", generator.getValueKey());

  }

  @Test
  public void testGeneratorNoHeader() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.NO_HEADER, "h", "d");
    Record record = RecordCreator.create();
    List<Field> list = new ArrayList<>();
    Map<String,Field> map = new HashMap<>();
    map.put("h", Field.create("A"));
    map.put("d", Field.create("a"));
    list.add(Field.create(map));
    map.put("h", Field.create("B"));
    map.put("d", Field.create("b"));
    list.add(Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    gen.close();
    Assert.assertEquals("a,b\r\n", writer.toString());
  }

  @Test
  public void testGeneratorIgnoreHeader() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.IGNORE_HEADER, "h", "d");
    Record record = RecordCreator.create();
    List<Field> list = new ArrayList<>();
    Map<String,Field> map = new HashMap<>();
    map.put("h", Field.create("A"));
    map.put("d", Field.create("a"));
    list.add(Field.create(map));
    map.put("h", Field.create("B"));
    map.put("d", Field.create("b"));
    list.add(Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    gen.close();
    Assert.assertEquals("a,b\r\n", writer.toString());
  }

  @Test
  public void testGeneratorWithHeader() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.WITH_HEADER, "h", "d");
    Record record = RecordCreator.create();
    List<Field> list = new ArrayList<>();
    Map<String,Field> map = new HashMap<>();
    map.put("h", Field.create("A"));
    map.put("d", Field.create("a"));
    list.add(Field.create(map));
    map.put("h", Field.create("B"));
    map.put("d", Field.create("b"));
    list.add(Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    map.put("d", Field.create("bb"));
    list.set(1, Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    gen.close();
    Assert.assertEquals("A,B\r\na,b\r\na,bb\r\n", writer.toString());
  }

  @Test
  public void testFlush() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.NO_HEADER, "h", "d");
    Record record = RecordCreator.create();
    List<Field> list = new ArrayList<>();
    Map<String,Field> map = new HashMap<>();
    map.put("h", Field.create("A"));
    map.put("d", Field.create("a"));
    list.add(Field.create(map));
    map.put("h", Field.create("B"));
    map.put("d", Field.create("b"));
    list.add(Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    gen.flush();
    Assert.assertEquals("a,b\r\n", writer.toString());
    gen.close();
  }

  @Test
  public void testClose() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.NO_HEADER, "h", "d");
    Record record = RecordCreator.create();
    List<Field> list = new ArrayList<>();
    Map<String,Field> map = new HashMap<>();
    map.put("h", Field.create("A"));
    map.put("d", Field.create("a"));
    list.add(Field.create(map));
    map.put("h", Field.create("B"));
    map.put("d", Field.create("b"));
    list.add(Field.create(map));
    record.set(Field.create(list));
    gen.write(record);
    gen.close();
    gen.close();
  }

  @Test(expected = IOException.class)
  public void testWriteAfterClose() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.NO_HEADER, "h", "d");
    gen.close();
    Record record = RecordCreator.create();
    gen.write(record);
  }

  @Test(expected = IOException.class)
  public void testFlushAfterClose() throws Exception {
    StringWriter writer = new StringWriter();
    DataGenerator gen = new DelimitedDataGenerator(writer, CsvMode.CSV.getFormat(), CsvHeader.NO_HEADER, "h", "d");
    gen.close();
    gen.flush();
  }

}
