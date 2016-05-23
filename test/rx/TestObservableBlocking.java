package rx;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Sets;

import rx.schedulers.Schedulers;
import util.AbstractTest;

public class TestObservableBlocking extends AbstractTest {
    
    @Test
    public void testFromCallable_NoSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            throwTestException("The exception is not raised since we are not acting on the BlockingObservable created in this test");    
            
            return UUID.randomUUID();
        })
        .toBlocking();
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test(expected = MockTestException.class)
    public void testFromCallable_ToBlockingSingle() {
        speedInMillis = 1000;
        
        try {
            
            Observable.fromCallable(() -> {
                
                callSlowService();
                
                throwTestException("This exception is exposed");    
                
                return UUID.randomUUID();
            })
            .toBlocking()
            .single();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }
    }
    
    @Test(expected = MockTestException.class)
    public void testDefer_ToBlockingSingle_ThrowsException() {
        speedInMillis = 1000;
        
        try {
            
            Observable.defer(() -> {
                
                callSlowService();
                
                throwTestException("This exception is exposed");    
                
                return Observable.fromCallable(() -> UUID.randomUUID());
            })
            .toBlocking()
            .single();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));   
        }
        
    }
    
    @Test(expected=MockTestException.class)
    public void testDefer_ToBlockingSingle_ThrowsException_In_InnerObservable() {
        speedInMillis = 1000;
        
        try {
            
            Observable.defer(() -> {
                
                return Observable.fromCallable(() -> {
                    
                    callSlowService();
                    
                    throwTestException("This exception is exposed");    

                    return UUID.randomUUID();
                    });
            })
            .toBlocking()
            .single();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }
        
    }
    
    @Test
    public void testFromCallable_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            return UUID.randomUUID();
        })
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testFromCallable_WithSubscription_Redo() {
        speedInMillis = 500;
        
        Set<String> stringSet = Sets.newLinkedHashSet();
        Observable<UUID> observable = Observable.fromCallable(() -> {
            
            callSlowService();
            
            return UUID.randomUUID();
        });
        
        stringSet.add(observable
                .map(guid -> guid.toString()) 
                .toBlocking()
                .single()
        );
        
        stringSet.add(observable
                .map(guid -> guid.toString()) 
                .toBlocking()
                .single()
        );
        
        watch.stop();
        
        assertThat(stringSet, hasSize(2));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * 2));
        
    }
    
    @Test
    public void testFromCallable_WithSubscription_Redo_Create() {
        speedInMillis = 500;
        
        Set<String> stringSet = Sets.newLinkedHashSet();
        Observable<UUID> observable = Observable.create(subscribier -> {
            callSlowService();
            subscribier.onNext(UUID.randomUUID());
            subscribier.onCompleted();
        });
        
        stringSet.add(observable
                .map(guid -> guid.toString()) 
                .toBlocking()
                .single()
        );
        
        stringSet.add(observable
                .map(guid -> guid.toString()) 
                .toBlocking()
                .single()
        );
        
        watch.stop();
        
        assertThat(stringSet, hasSize(2));
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis * 2));
        
    }
    
    @Test
    public void testDefer_NoSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                return UUID.randomUUID();
            });
        })
        .toBlocking()
        .single();
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testDefer_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                return UUID.randomUUID();
            });
        })
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testDefer_ThreadedSubscription_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            deferThreadName();
            
            return Observable.fromCallable(() -> {
                
                callableThreadName();
                
                callSlowService();
                
                return UUID.randomUUID();
            }).subscribeOn(Schedulers.io());
        })
        .toBlocking();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test
    public void testDefer_ThreadedObserver_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            deferThreadName();
            
            return Observable.fromCallable(() -> {
                
                callableThreadName();
                
                callSlowService();
                
                return UUID.randomUUID();
            }).observeOn(Schedulers.io());
            
        })
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testDefer_Observer_ObserverNotThreaded_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            deferThreadName();
            
            return Observable.fromCallable(() -> {
                
                callableThreadName();
                
                return UUID.randomUUID();
            }).doOnNext(guid -> {
                
                callSlowService();
                
            });
            
        })
        .toBlocking()
        .single();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testDefer_Observer_Threaded_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            deferThreadName();
            
            return Observable.fromCallable(() -> {
                
                callableThreadName();
                
                return UUID.randomUUID();
            })
            .observeOn(Schedulers.io())
            .doOnNext(guid -> {
                
                // this part of the Observable will run in a thread 
                observerThreadName();
                
                callSlowService();
                
            });
            
        })
        .toBlocking();
        
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test
    public void testCallable_Observer_NotThreaded_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callableThreadName();
        
            return UUID.randomUUID();
        })
        .doOnNext(guid -> {
            
            // this part of the Observable will run in a thread 
            observerThreadName();
            
            callSlowService();
        })
        .toBlocking()
        .single();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testCallable_Observer_Threaded_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callableThreadName();
        
            return UUID.randomUUID();
        })
        .observeOn(Schedulers.io())
        .doOnNext(guid -> {
            // this part of the Observable will run in a thread 
            observerThreadName();
            callSlowService();
        })
        .toBlocking()
        .single();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testCallable_Threaded_WithSubscription_SlowSubcriber() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callableThreadName();
            callSlowService();
        
            return UUID.randomUUID();
        })
        .subscribeOn(Schedulers.io())
        .doOnNext(guid -> {
            // this part of the Observable will run in a thread 
            observerThreadName();
        })
        .toBlocking()
        .single();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testCallable_Threaded_WithSubscription_SlowObserver() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callableThreadName();
        
            return UUID.randomUUID();
        })
        .subscribeOn(Schedulers.io())
        .doOnNext(guid -> {
            // this part of the Observable will run in a thread 
            observerThreadName();
            callSlowService();
        })
        .toBlocking()
        .single();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
}
