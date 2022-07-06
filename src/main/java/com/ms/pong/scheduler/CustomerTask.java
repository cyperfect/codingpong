package com.ms.pong.scheduler;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import com.ms.pong.customer.FileCustomer;
import com.ms.pong.model.MessageVo;
import com.ms.pong.producer.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class CustomerTask {


    @Scheduled(fixedRate = 2000)
    public void schCustomerTask() throws InterruptedException {

        //读取目录下所有文件
        String filepath = "D:\\message\\";
        String movepath = "D:\\message1\\";
        File file = new File(filepath);
        String[] filelist = file.list();
        if (filelist == null || filelist.length == 0) {
            return;
        }

        // 创建ringbuffer
        RingBuffer<MessageVo> buffer = RingBuffer.create(ProducerType.SINGLE, new EventFactory<MessageVo>() {
            @Override
            public MessageVo newInstance() {
                return new MessageVo();
            }
        }, 1024 * 1024, new YieldingWaitStrategy());

        SequenceBarrier barrier = buffer.newBarrier();

        FileCustomer fileCustomer = new FileCustomer();
        //
        WorkerPool<MessageVo> workerPool = new WorkerPool<MessageVo>(buffer, barrier, new IgnoreExceptionHandler(), fileCustomer);

        //创建线程池
        ExecutorService executors = Executors.newFixedThreadPool(10);
        workerPool.start(executors);
        CountDownLatch countDownLatch = new CountDownLatch(1);


        List<String> fList = new ArrayList<String>(Arrays.asList(filelist));

        // 读取文件内容
        int toIndex = 6;
        List<CompletableFuture<List<MessageVo>>> futures = new ArrayList<>();
        for (int i = 0; i < fList.size(); i += 6) {

            if (i + 6 > fList.size()) {
                toIndex = fList.size() - i;
            }
            List<String> subFiles = fList.subList(i, i + toIndex);
            CompletableFuture<List<MessageVo>> future = CompletableFuture
                    .supplyAsync(() -> {
                        List<MessageVo> results = new ArrayList<>(subFiles.size());
                        for (String subFile : subFiles) {
                            //读取文件
                            File readfile = new File(filepath + subFile);
                            InputStreamReader isr = null;
                            try {
                                isr = new InputStreamReader(new FileInputStream(readfile), "UTF-8");
                                BufferedReader reader = new BufferedReader(isr);
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    results.add(new MessageVo(line, subFile));
                                }
                                reader.close();
                                isr.close();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //读取完后， 移动文件位置
                            readfile.renameTo(new File(movepath + readfile.getName()));
                        }

                        return results;
                    })
                    .exceptionally(e -> {
                        return null;
                    });
            futures.add(future);
        }
        List<MessageVo> messageVoLists = new ArrayList<>(fList.size());
        for (CompletableFuture<List<MessageVo>> future : futures) {
            try {
                List<MessageVo> messageVoList = future.get();
                messageVoLists.addAll(messageVoList);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        // 放入生产者生产
        int toIndexs = 6;
        for (int i = 0; i < messageVoLists.size(); i +=6) {
            if (i + 6 > fList.size()) {
                toIndexs = fList.size() - i;
            }
            List<MessageVo> messageVos1 = messageVoLists.subList(i, i + toIndexs);

            if (messageVos1.size() < 1) {
                continue;
            }
            Producer producer = new Producer(buffer);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    messageVos1.stream().forEach(f ->
                            producer.setData(f.getText(), f.getFileName()));
                }
            }).start();
        }

        Thread.sleep(1000);
        countDownLatch.countDown();
        Thread.sleep(1000);

        workerPool.halt();
        executors.shutdown();
    }
}
