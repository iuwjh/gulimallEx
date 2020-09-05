package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest()
class GulimallSearchApplicationTests {
    @Autowired
    RestHighLevelClient client;

    @Test
    void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();

        searchRequest.indices("bank");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {

        }

    }

    @Test
    void contextLoads() throws IOException {
        // System.out.println(esClient);
        IndexRequest indexRequest = new IndexRequest("user1");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("零零");
        user.setGender("M");
        user.setAge(33);
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);

        IndexResponse index = client.index(indexRequest, GulimallElasticsearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Data
    private static class User {
        private String username;
        private String gender;
        private Integer age;
    }
}
