package io.github.lhr;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class FluxArray<T> extends Flux<T> {
    private T[] array;

    public FluxArray(T[] data) {
        this.array = data;
    }

    @Override
    public void subscribe(Subscriber<? super T> actual) {
        actual.onSubscribe(new ArraySubscription<>(actual, array));
    }

    /**
     * 订阅期：
     * 1.当使用.subscribe订阅这个发布者时，首先会new一个具有相应逻辑的Subscription（如ArraySubscription，这个Subscription定义了如何处理下游的request，以及如何“发出数据”）；
     * 2然后发布者将这个Subscription通过订阅者的.onSubscribe方法传给订阅者；
     * 3在订阅者的.onSubscribe方法中，需要通过Subscription发起第一次的请求.request；
     * 4Subscription收到请求，就可以通过回调订阅者的onNext方法发出元素了，有多少发多少，但不能超过请求的个数；
     * 5订阅者在onNext中通常定义对元素的处理逻辑，处理完成之后，可以继续发起请求；
     * 6发布者根据继续满足订阅者的请求；
     * 7直至发布者的序列结束，通过订阅者的onComplete予以告知；当然序列发送过程中如果有错误，则通过订阅者的onError予以告知并传递错误信息；这两种情况都会导致序列终止，订阅过程结束。
     * @param <T>
     */
    static class ArraySubscription<T> implements Subscription {
        final Subscriber<? super T> actual;
        //Subscription内也有一份数据
        final T[] array;
        int index;
        boolean canceled;

        public ArraySubscription(Subscriber<? super T> actual, T[] array) {
            this.actual = actual;
            this.array = array;
        }

        @Override
        public void request(long n) {
            if (canceled) {
                return;
            }
            //当有可以发出的元素时，回调订阅者的onNext方法传递元素
            long length = array.length;
            for (int i = 0; i < n && index < length; i++) {
                if (canceled) {
                    return;
                }
                actual.onNext(array[index++]);
            }
            //当所有的元素都发完时，回调订阅者的onComplete方法；
            if (index == length) {
                actual.onComplete();
            }
        }

        @Override
        public void cancel() {
            this.canceled = true;
        }
    }
}