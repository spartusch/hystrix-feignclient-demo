package com.github.spartusch.hfdemo.service;

import rx.Single;

public interface DemoService {

    String getData();

    String getDataWithDecode404();

    String getDataWithFallback();

    String getDataWithErrorDecoder();

    Single<String> getDataWithRx();

}
