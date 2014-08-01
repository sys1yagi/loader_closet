package com.sys1yagi.loadercloset;

import android.content.Context;
import android.support.v4.content.Loader;
import android.test.ActivityUnitTestCase;
import android.view.ContextThemeWrapper;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LoaderClosetTest extends ActivityUnitTestCase<MockActivity> {

    public LoaderClosetTest() {
        super(MockActivity.class);
    }

    private MockActivity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContextThemeWrapper context = new ContextThemeWrapper(
                getInstrumentation().getContext(), R.style.Theme_AppCompat);

        setActivityContext(context);
        activity = launchActivity(
                getInstrumentation().getTargetContext().getPackageName(),
                MockActivity.class, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        activity.finish();
    }

    public void testOneshotLoader() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoader(new MockOneshotLoader(activity, "success", 100),
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                assertEquals("success", result);
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }
                        }
                );
                loaderCloset.start();

                //see here END

            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    public void testErrorHandling() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoader(
                        new MockOneshotLoader(activity, new Exception("error"), 100),
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                assertEquals("error", failed.getMessage());
                                countDownLatch.countDown();
                            }
                        }
                );
                loaderCloset.start();

                //see here END

            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    public void testAnonymouseOneshotLoader() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoader(
                        new OneShotLoader(activity) {
                            @Override
                            public LoaderResult loadInBackground(LoaderResult takeOver) {
                                return success("success");
                            }
                        },
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                assertEquals("success", result);
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }
                        }
                );
                loaderCloset.start();

                //see here END

            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //parallel
    public void testParallelSerialTiming() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success2", 300));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success3", 600));
                loaderCloset.startParallel(new UiThreadParallelReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        assertNotNull(loaders);
                        assertEquals(3, loaders.size());

                        assertNotNull(results);
                        assertEquals(3, results.size());

                        assertEquals("success1", results.get(0).getSuccess());
                        assertEquals("success2", results.get(1).getSuccess());
                        assertEquals("success3", results.get(2).getSuccess());

                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //parallel random timing
    public void testParallelRandomTiming() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success2", 50));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success3", 200));
                loaderCloset.startParallel(new UiThreadParallelReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        assertNotNull(loaders);
                        assertEquals(3, loaders.size());

                        assertNotNull(results);
                        assertEquals(3, results.size());

                        assertEquals("success1", results.get(0).getSuccess());
                        assertEquals("success2", results.get(1).getSuccess());
                        assertEquals("success3", results.get(2).getSuccess());

                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //parallel contain error
    public void testParallelError() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, new Exception("error"), 100));
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success3", 200));
                loaderCloset.startParallel(new UiThreadParallelReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        assertNotNull(loaders);
                        assertEquals(3, loaders.size());

                        assertNotNull(results);
                        assertEquals(3, results.size());

                        assertEquals("success1", results.get(0).getSuccess());

                        Exception exception = (Exception) results.get(1).getFailed();
                        assertEquals("error", exception.getMessage());

                        assertEquals("success3", results.get(2).getSuccess());

                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //parallel different result types;
    public void testParallelDifferentType() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForParallel(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForParallel(
                        new MockDifferentTypeOneshotLoader(activity));
                loaderCloset.startParallel(new UiThreadParallelReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        assertNotNull(loaders);
                        assertEquals(2, loaders.size());

                        assertNotNull(results);
                        assertEquals(2, results.size());

                        assertEquals("success1", results.get(0).getSuccess());
                        assertEquals(10, results.get(1).getSuccess());
                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //serial
    public void testSerial() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForSerial(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForSerial(
                        new MockSerialLoader(activity));
                loaderCloset.startSerial(new UiThreadSerialReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        assertNotNull(loaders);
                        assertNotNull(results);

                        assertEquals(2, results.size());
                        assertEquals("success1", results.get(0).getSuccess());
                        assertEquals("success1success1", results.get(1).getSuccess());
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onLoadFailed(List<Loader> completedLoaders, Loader failLoader,
                            LoaderResult fail) {
                        fail("This method should not be called.");
                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //serial has error
    public void testSerialHasError() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                //see here BEGIN

                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
                loaderCloset.registerLoaderForSerial(
                        new MockOneshotLoader(activity, "success1", 100));
                loaderCloset.registerLoaderForSerial(
                        new MockSerialLoader(activity, new Exception("error")));
                loaderCloset.startSerial(new UiThreadSerialReceiver() {
                    @Override
                    public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                        fail("This method should not be called.");
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onLoadFailed(List<Loader> completedLoaders, Loader failLoader,
                            LoaderResult fail) {
                        assertEquals(1, completedLoaders.size());
                        assertNotNull(failLoader);
                        Exception exception = (Exception) fail.getFailed();
                        assertEquals("error", exception.getMessage());
                        countDownLatch.countDown();
                    }
                });

                //see here END

            }
        });

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //reload
    public void testReload() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
        loaderCloset
                .registerLoader(new MockReloadLoader(activity, "success"),
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                countDownLatch.countDown();
                                if (countDownLatch.getCount() == 0) {
                                    assertEquals("success2", result);
                                } else {
                                    loaderCloset.reload();
                                }
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }
                        }
                );
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                loaderCloset.start();
            }
        });

        countDownLatch.await(1, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //pageable
    public void testPageable() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(6);

        final LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
        final PageableLoader pageableLoader = new MockPageableLoader(activity, 5);
        final UiThreadPageableReceiver<String, Exception> pageableReceiver
                = new UiThreadPageableReceiver<String, Exception>() {
            @Override
            public void onEndOfData() {
                countDownLatch.countDown();
            }

            @Override
            public void onLoadFinished(Loader loader, String result) {
                countDownLatch.countDown();
                assertEquals(result, "page" + (5 - countDownLatch.getCount()));

                loaderCloset.startNextPage(pageableLoader, this);
            }

            @Override
            public void onLoadFailed(Loader loader, Exception failed) {
                fail("This method should not be called.");
                countDownLatch.countDown();
            }
        };
        loaderCloset
                .registerLoader(pageableLoader,
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                countDownLatch.countDown();
                                loaderCloset.startNextPage(pageableLoader, pageableReceiver);
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }
                        }
                );
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                loaderCloset.start();
            }
        });

        countDownLatch.await(1, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //variations
    //oneshot and parallel
    public void testOneshotAndParallel() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());

                doOneshot(loaderCloset, countDownLatch);
                doParallel(loaderCloset, countDownLatch);
            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //oneshot and serial
    public void testOneshotAndSerial() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());

                doOneshot(loaderCloset, countDownLatch);
                doSerial(loaderCloset, countDownLatch);
            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //serial and parallel
    public void testSerialAndParallel() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());

                doSerial(loaderCloset, countDownLatch);
                doParallel(loaderCloset, countDownLatch);
            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //oneshot and serial and parallel
    public void testOneshotAndSerialAndParallel() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(3);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());

                doOneshot(loaderCloset, countDownLatch);
                doSerial(loaderCloset, countDownLatch);
                doParallel(loaderCloset, countDownLatch);
            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
    }

    //reload parallel
    public void testReloadAndParallel() throws Throwable {
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        final CountDownLatch countDownLatchForParallel = new CountDownLatch(1);
        final LoaderCloset loaderCloset = new LoaderCloset(activity.getSupportLoaderManager());
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                loaderCloset.registerLoader(new MockReloadLoader(activity, "success"),
                        new UiThreadReceiver<String, Exception>() {
                            @Override
                            public void onLoadFinished(Loader loader, String result) {
                                countDownLatch.countDown();
                                if (countDownLatch.getCount() == 0) {
                                    assertEquals("success2", result);
                                } else {
                                    loaderCloset.reload();
                                }
                            }

                            @Override
                            public void onLoadFailed(Loader loader, Exception failed) {
                                fail("This method should not be called.");
                                countDownLatch.countDown();
                            }
                        }
                );
                doParallel(loaderCloset, countDownLatchForParallel);
                loaderCloset.start();
            }
        });

        countDownLatch.await(2, TimeUnit.SECONDS);
        countDownLatchForParallel.await(2, TimeUnit.SECONDS);
        assertEquals(0, countDownLatch.getCount());
        assertEquals(0, countDownLatchForParallel.getCount());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //methods for tests.
    private void doOneshot(LoaderCloset loaderCloset, final CountDownLatch countDownLatch) {
        loaderCloset.registerLoader(
                new MockOneshotLoader(activity, "success0", 100),
                new UiThreadReceiver<String, Exception>() {
                    @Override
                    public void onLoadFinished(Loader loader, String result) {
                        assertEquals("success0", result);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onLoadFailed(Loader loader, Exception failed) {
                        fail("This method should not be called.");
                        countDownLatch.countDown();
                    }
                }
        );
        loaderCloset.start();
    }

    private void doSerial(LoaderCloset loaderCloset, final CountDownLatch countDownLatch) {
        loaderCloset.registerLoaderForSerial(
                new MockOneshotLoader(activity, "success1", 100));
        loaderCloset.registerLoaderForSerial(
                new MockSerialLoader(activity));
        loaderCloset.startSerial(new UiThreadSerialReceiver() {
            @Override
            public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                assertNotNull(loaders);
                assertNotNull(results);

                assertEquals(2, results.size());
                assertEquals("success1", results.get(0).getSuccess());
                assertEquals("success1success1", results.get(1).getSuccess());
                countDownLatch.countDown();
            }

            @Override
            public void onLoadFailed(List<Loader> completedLoaders, Loader failLoader,
                    LoaderResult fail) {
                fail("This method should not be called.");
                countDownLatch.countDown();
            }
        });
    }

    private void doParallel(LoaderCloset loaderCloset, final CountDownLatch countDownLatch) {
        loaderCloset.registerLoaderForParallel(
                new MockOneshotLoader(activity, "success1", 100));
        loaderCloset.registerLoaderForParallel(
                new MockOneshotLoader(activity, "success2", 300));
        loaderCloset.registerLoaderForParallel(
                new MockOneshotLoader(activity, "success3", 600));
        loaderCloset.startParallel(new UiThreadParallelReceiver() {
            @Override
            public void onLoadFinished(List<Loader> loaders, List<LoaderResult> results) {
                assertNotNull(loaders);
                assertEquals(3, loaders.size());

                assertNotNull(results);
                assertEquals(3, results.size());

                assertEquals("success1", results.get(0).getSuccess());
                assertEquals("success2", results.get(1).getSuccess());
                assertEquals("success3", results.get(2).getSuccess());

                countDownLatch.countDown();
            }
        });
    }


    //mock classes for tests.
    static class MockOneshotLoader extends OneShotLoader<String, Exception> {

        String result = null;

        Exception exception = null;

        long waitTime = 0L;

        public MockOneshotLoader(Context context, String result, long waitTime) {
            super(context);
            this.result = result;
            this.waitTime = waitTime;
        }

        public MockOneshotLoader(Context context, Exception exception, long waitTime) {
            super(context);
            this.exception = exception;
            this.waitTime = waitTime;
        }

        @Override
        public LoaderResult loadInBackground(LoaderResult takeover) {
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
                return failed(e);
            }
            if (exception != null) {
                return failed(exception);
            } else {
                return success(result);
            }
        }
    }

    static class MockDifferentTypeOneshotLoader extends OneShotLoader<Integer, Exception> {

        public MockDifferentTypeOneshotLoader(Context context) {
            super(context);
        }

        @Override
        public LoaderResult loadInBackground(LoaderResult takeover) {
            return success(10);
        }
    }

    static class MockSerialLoader extends OneShotLoader<String, Exception> {

        Exception exception;

        public MockSerialLoader(Context context) {
            super(context);
        }

        public MockSerialLoader(Context context, Exception exception) {
            super(context);
            this.exception = exception;
        }

        @Override
        public LoaderResult loadInBackground(LoaderResult takeover) {
            if (exception != null) {
                return failed(exception);
            }
            String text = takeover.getSuccess().toString();
            return success(text + text);
        }
    }

    static class MockReloadLoader extends OneShotLoader<String, Exception> {

        int times = 0;

        String result;

        Exception exception;

        public MockReloadLoader(Context context, String result) {
            super(context);
            this.result = result;
        }

        public MockReloadLoader(Context context, String result, Exception exception) {
            super(context);
            this.result = result;
            this.exception = exception;
        }

        @Override
        public LoaderResult loadInBackground(LoaderResult takeover) {
            if (exception != null) {
                Exception exception1 = exception;
                exception = null;
                return failed(exception1);
            }
            times++;
            return success(result + times);
        }
    }

    static class MockPageableLoader extends PageableLoader<String, Exception> {

        int remainPage;

        int page;

        public MockPageableLoader(Context context, int remainPage) {
            super(context);
            this.remainPage = remainPage - 1;
            this.page = 1;
        }

        @Override
        public boolean prepareNextPage() {
            remainPage--;
            page++;
            return remainPage >= 0;
        }

        @Override
        public LoaderResult loadInBackground(LoaderResult takeOver) {
            return success("page" + page);
        }
    }
}
