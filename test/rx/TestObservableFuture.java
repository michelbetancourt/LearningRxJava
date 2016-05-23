package rx;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rx.exceptions.OnErrorNotImplementedException;
import rx.schedulers.Schedulers;
import util.AbstractTest;

public class TestObservableFuture extends AbstractTest {
    
    @Test(expected=OnErrorNotImplementedException.class)
    public void testObservable_Future_Timeout() {
        speedInMillis = 1000;
        
        CompletableFuture<UUID> future = new CompletableFuture<>();
        
        try {
            Observable.from(future)
            .timeout(speedInMillis, TimeUnit.MILLISECONDS)
            .subscribe();
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
            
        }
        
    }
    
    @Test(expected=MockTestException.class)
    public void testObservable_Future_Timeout_ImplementsOnErrorReturn() {
        speedInMillis = 1000;
        
        CompletableFuture<UUID> future = new CompletableFuture<>();
        
        try {
            Observable.from(future)
            .timeout(speedInMillis, TimeUnit.MILLISECONDS)
            .onErrorReturn(throwable -> {
                // timeout is reached here
                // using onErrorReturn to assert specific exception
                throw new MockTestException(throwable);
            })
            .toBlocking()
            .single();    
        } finally {
            watch.stop();            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
            
        }
        
    }
    
    @Test
    public void testObservable_Future_With_Background_Process_Finishing_Future() throws Exception {
        speedInMillis = 2000;
        
        CompletableFuture<UUID> future = new CompletableFuture<>();
        
        try {
            
            // set the future in the background, delaying first
            Observable.fromCallable(()-> UUID.randomUUID())
                .subscribeOn(Schedulers.io())
                .delay(speedInMillis, TimeUnit.MILLISECONDS)
                .doOnNext(uuid -> future.complete(uuid))
                .subscribe();
            
            // wait the the future to complete here
            // place it on a scheduler since a future is a blocking construct
            // for an Observable
            strings = Observable.from(future, Schedulers.io())
            .timeout(speedInMillis + speedInMillis / 2, TimeUnit.MILLISECONDS)
            .map(uuid -> {
                showCallableThreadName();
                return uuid.toString();
            })
            .toList()
            .toBlocking()
            .single();    
        } finally {
            watch.stop();      
            assertThat(strings, is(singletonList(future.get().toString())));
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
            
        }
        
    }
    
       
}
