package io.github.lhr;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class FluxTests {

    @Test
    public void fluxArrayTest() {
        Flux.just(1, 2, 3, 4, 5).subscribe(new Subscriber<Integer>() { // 1

            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                //订阅时请求5个元素
                s.request(5);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext:" + integer);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        });
    }

    @Test
    public void lambdaSubscriberTest() {
        Flux.just(1, 2, 3, 4, 5)
                .map(i -> i * 2)
                .subscribe(
                        System.out::println,
                        System.err::println,
                        () -> System.out.println("Completed.")
                );
    }
}
