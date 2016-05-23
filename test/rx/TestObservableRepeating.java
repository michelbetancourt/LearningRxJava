package rx;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import rx.schedulers.Schedulers;
import util.AbstractTest;

public class TestObservableRepeating extends AbstractTest {
    
    @Test
    public void testFromCallable_NoSubscription() {
        speedInMillis = 1000;
        repeatingCount = 10;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            throwTestException("The exception is not raised since we are not acting on the BlockingObservable created in this test");    
            
            return UUID.randomUUID();
        })
        .repeat(repeatingCount)
        .toBlocking();
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test(expected = MockTestException.class)
    public void testFromCallable_ToBlockingSingle() {
        speedInMillis = 1000;
        repeatingCount = 10;
        
        try {
            
            Observable.fromCallable(() -> {
                
                callSlowService();
                
                throwTestException("This exception is exposed");    
                
                return UUID.randomUUID();
            })
            .repeat(repeatingCount)
            .toBlocking()
            .single();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), both(greaterThanOrEqualTo(speedInMillis)).and(lessThan(speedInMillis * repeatingCount)));
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFromCallable_ToBlockingSingle_Of_MultipleResults() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        try {
            
            Observable.fromCallable(() -> {
                
                callSlowService();    
                
                return UUID.randomUUID();
            })
            .repeat(repeatingCount)
            .toBlocking()
            .single();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }
    }
    
    @Test
    public void testFromCallable_WithSubscription() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            return UUID.randomUUID();
        })
        .repeat(repeatingCount)
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test
    public void testDefer_NoSubscription() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        strings = Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                return UUID.randomUUID();
            });
        })
        .repeat(repeatingCount)
        .map(uuid -> uuid.toString())
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(strings, hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test
    public void testDefer_RepeatZero() {
        speedInMillis = 500;
        repeatingCount = 0;
        
        strings = Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                return UUID.randomUUID();
            });
        })
        .repeat(repeatingCount)
        .map(uuid -> {
            throwTestException("This line is not reached since the Observable will not return any elements");
            return uuid.toString();
        })
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(strings, hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test(expected = MockTestException.class)
    public void testDefer_Repeating_ThrowsException_On_Transformation() {
        speedInMillis = 50;
        repeatingCount = 5;
        
        try {
            strings = Observable.defer(() -> {
                return Observable.fromCallable(() -> {
                    
                    callSlowService();
                    
                    return UUID.randomUUID();
                });
            })
            .repeat(repeatingCount)
            .map(uuid -> {
                throwTestException("This line is not reached since the Observable will not return any elements");
                return uuid.toString();
            })
            .toList()
            .toBlocking()
            .single();
        } finally {
            
            watch.stop();
            assertThat(strings, nullValue());
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
            
        }
       
        
        
    }
    
    @Test
    public void testDefer_Repeating_Threaded_Sync() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        strings = Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                showCallableThreadName();
                callSlowService();
                
                return UUID.randomUUID();
            }).subscribeOn(Schedulers.io());
        })
        .repeat(repeatingCount)
        .map(uuid -> uuid.toString())
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(strings, hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test
    public void testDefer_Repeating_Threaded_Observer_Sync() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        strings = Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                showCallableThreadName();
                callSlowService();
                
                return UUID.randomUUID();
            }).observeOn(Schedulers.io());
        })
        .repeat(repeatingCount)
        .map(uuid -> uuid.toString())
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(strings, hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test
    public void testDefer_Repeating_Create_Threaded_Sync() {
        speedInMillis = 500;
        repeatingCount = 3;
        
        strings = Observable.defer(()->{
          return  Observable.create(subscriber ->{
              showCallableThreadName();
              UUID uuid = callSlowService();
              subscriber.onNext(uuid);
              subscriber.onCompleted();
          }).subscribeOn(Schedulers.io());  
        })
        .subscribeOn(Schedulers.io())
        .repeat(repeatingCount, Schedulers.io())
        .map(uuid -> uuid.toString())
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(strings, hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * repeatingCount));
        
    }
    
    @Test
    public void testDefer_Repeating_Threaded_Observer_Async() {
        speedInMillis = 1000;
        repeatingCount = 50;
        
        List<Observable<UUID>> observables = Lists.newLinkedList();
        
        Observable<UUID> slow = Observable.fromCallable(() -> {
            
            showCallableThreadName();
            callSlowService();
            
            return UUID.randomUUID();
        });
        
        Observable.fromCallable(()->{
            
          observables.add(slow.subscribeOn(Schedulers.io()));
          
          return null;  
        }).repeat(repeatingCount)
        .toList()
        .toBlocking()
        .single();
                
        strings = Observable.merge(observables)
        .map(uuid -> uuid.toString())
        .toList()
        .toBlocking()
        .single();
        
        watch.stop();
        
        assertThat(Sets.newLinkedHashSet(strings), hasSize(repeatingCount));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), both(greaterThanOrEqualTo(speedInMillis)).and(lessThan(speedInMillis * 2 - speedInMillis / 2)));
        
    }
    
}
