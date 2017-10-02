package com.github.spartusch.hfdemo.service;

import rx.Single;

public interface DemoService {

    String getData();

    String getDataWithFallback();

    String getDataWithErrorDecoder();

    Single<String> getRxData();

}
