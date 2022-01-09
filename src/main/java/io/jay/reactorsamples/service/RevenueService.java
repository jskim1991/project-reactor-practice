package io.jay.reactorsamples.service;


import io.jay.reactorsamples.domain.Revenue;

import static io.jay.reactorsamples.util.CommonUtil.delay;

public class RevenueService {

    public Revenue getRevenue(Long movieId){
        delay(1000); // simulating a network call ( DB or Rest call)
        return Revenue.builder()
                .movieInfoId(movieId)
                .budget(1000000)
                .boxOffice(5000000)
                .build();

    }
}
