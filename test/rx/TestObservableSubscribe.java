package rx;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.exceptions.OnErrorNotImplementedException;
import rx.schedulers.Schedulers;
import util.AbstractTest;

public class TestObservableSubscribe extends AbstractTest {
    
    @Test
    public void testFromCallable_NoSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            throwTestException("The next line is not reached since the observerable is not subscribed");    
            
            return UUID.randomUUID();
        });
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test(expected = OnErrorNotImplementedException.class)
    public void testFromCallable_WithException_And_Subscription() {
        speedInMillis = 1000;
        
        try {
            Observable.fromCallable(() -> {
                callSlowService();
                
                throwTestException("An exception will be raised and exposed.  The next line is not reached either.");    
                
                return UUID.randomUUID();
            })
            .subscribe();
            
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }

    }
    
    @Test(expected = OnErrorNotImplementedException.class)
    public void testDefer_WithException_And_Subscription() {
        speedInMillis = 1000;
        
        try {
            Observable.defer(() -> {
                
                callSlowService();
                
                throwTestException("An exception will be raised and exposed.  The next line is not reached either.");    
                
                return Observable.fromCallable(() -> UUID.randomUUID());
            })
            .subscribe();
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }
        
        
       
        
    }
    
    @Test
    public void testDefer_WithException_And_Threaded_Subscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            callSlowService();
            
            throwTestException("An exception is not raised here.  The next line is not reached.");    
            
            return Observable.fromCallable(() -> UUID.randomUUID());
        })
        .subscribeOn(Schedulers.io())
        .subscribe();
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test(expected = OnErrorNotImplementedException.class)
    public void testDefer_WithException_InnerObservable_And_Subscription() {
        speedInMillis = 1000;
        
        try {
            Observable.defer(() -> {
                
                return Observable.fromCallable(() -> {
                    
                    callSlowService();
                    
                    throwTestException("An exception will be raised and exposed.  The next line is not reached.");
                    
                    return UUID.randomUUID();
                });
            })
            .subscribe();
        } finally {
            watch.stop();
            
            assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        }
        
    }
    
    @Test
    public void testDefer_WithException_InnerObservable_And_ThreadedSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                throwTestException("An exception will not be raised and exposed.  The next line is not reached.");
                
                return UUID.randomUUID();
            });
        })
        .subscribeOn(Schedulers.io())
        .subscribe();
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
    @Test
    public void testFromCallable_WithSubscription() {
        speedInMillis = 1000;
        
        Observable.fromCallable(() -> {
            
            callSlowService();
            
            return UUID.randomUUID();
        })
        .subscribe();
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), greaterThanOrEqualTo(speedInMillis));
        
    }
    
    @Test
    public void testDefer_NoSubscription() {
        speedInMillis = 1000;
        
        Observable.defer(() -> {
            
            return Observable.fromCallable(() -> {
                
                callSlowService();
                
                return UUID.randomUUID();});
        });
        
        watch.stop();
        
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
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
        .subscribe();
        
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
            })
            .subscribeOn(Schedulers.io());
        })
        .subscribe();
        
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
            
        }).subscribe();
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
            
        }).subscribe();
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
            
        }).subscribe();
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
        }).subscribe();
            
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
        }).subscribe();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
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
        }).subscribe();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
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
        }).subscribe();
            
        watch.stop();
        assertThat(watch.elapsed(TimeUnit.MILLISECONDS), lessThan(speedInMillis));
        
    }
    
}
