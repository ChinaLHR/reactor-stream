package io.github.lhr;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Function;

public class FluxMap<T, R> extends Flux<R> {

    private final Flux<? extends T> source;
    private final Function<? super T, ? extends R> mapper;

    public FluxMap(Flux<? extends T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Subscriber<? super R> actual) {
        source.subscribe(new MapSubscriber<>(actual, mapper));
    }

    /**
     * Subscription：根据其订阅者的请求发出数据
     * Subscription：订阅上游的发布者，并将数据处理后传递给下游的订阅者
     * @param <T>
     * @param <R>
     */
    static final class MapSubscriber<T, R> implements Subscriber<T>, Subscription {
        private final Subscriber<? super R> actual;
        private final Function<? super T, ? extends R> mapper;

        boolean done;

        Subscription subscriptionOfUpstream;

        MapSubscriber(Subscriber<? super R> actual, Function<? super T, ? extends R> mapper) {
            this.actual = actual;
            this.mapper = mapper;
        }

        @Override
        public void onSubscribe(Subscription s) {
            //拿到来自上游的Subscription
            this.subscriptionOfUpstream = s;
            //回调下游的onSubscribe，将自身作为Subscription传递过去；
            actual.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            //收到上游发出的数据后，将其用mapper进行转换，然后接着发给下游；
            if (done) {
                return;
            }
            actual.onNext(mapper.apply(t));
        }

        @Override
        public void onError(Throwable t) {
            //将上游的错误信号原样发给下游；
            if (done) {
                return;
            }
            done = true;
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            //将上游的完成信号原样发给下游；
            if (done) {
                return;
            }
            done = true;
            actual.onComplete();
        }


        @Override
        public void request(long n) {
            //将下游的请求传递给上游；
            this.subscriptionOfUpstream.request(n);
        }

        @Override
        public void cancel() {
            //将下游的取消操作传递给上游。
            this.subscriptionOfUpstream.cancel();
        }
    }
}