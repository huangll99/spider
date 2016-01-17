package com.hll;

import rx.Observable;

/**
 * Created by hll on 2016/1/16.
 */
public class RxJavaPrint {
  public static void subsctribePrint(Observable<?> observable, String name) {
    observable.subscribe(
        item -> System.out.println(name + " : " + item),
        error -> {
          System.out.println(name + "got a error!");
          error.printStackTrace();
        },
        () -> System.out.println(name + " : completed!")
    );
  }
}
