package com.hll.zk;

import org.junit.Test;
import rx.Observable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by hll on 2016/1/16.
 */
public class RxjavaTest {
  @Test
  public void test(){
    Path resources = Paths.get("src", "main", "resources");
    try (DirectoryStream<Path> dStream
             = Files.newDirectoryStream(resources)) {
      Observable<Path> dirObservable = Observable.from(dStream);
      dirObservable.subscribe(System.out::println);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
