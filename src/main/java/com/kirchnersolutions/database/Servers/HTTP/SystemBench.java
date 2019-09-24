package com.kirchnersolutions.database.Servers.HTTP;

import com.kirchnersolutions.database.Configuration.SysVars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SystemBench {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private volatile SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private SysVars sysVars;

    private volatile AtomicBoolean primecount = new AtomicBoolean(false), primetime = new AtomicBoolean(false), hashcount = new AtomicBoolean(false), hashtime = new AtomicBoolean(false), randombytes = new AtomicBoolean(false), inttime = new AtomicBoolean(false), intcount = new AtomicBoolean(false), floattime = new AtomicBoolean(false);
    private volatile AtomicBoolean primecounte = new AtomicBoolean(false), primetimee = new AtomicBoolean(false), hashcounte = new AtomicBoolean(false), hashtimee = new AtomicBoolean(false), randombytese = new AtomicBoolean(false), inttimee = new AtomicBoolean(false), intcounte = new AtomicBoolean(false), floattimee = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);

    synchronized BigInteger cpuBench(int threads) throws Exception{
        return CPUBench(threads);
    }

    void stop(){
        stop.set(true);
    }

    private synchronized BigInteger CPUBench(int threads) throws Exception{
        long stime = System.currentTimeMillis();
        System.out.println("Starting CPU bench with " + threads + " threads");
        simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting CPU bench with " + threads + " threads");
        List<BigInteger> results = new ArrayList<>();
        BigInteger score = new BigInteger("0");
        Future<BigInteger>[] futures = new Future[threads];
        for(int i = 0; i < threads; i++){
            futures[i] = threadPoolTaskExecutor.submit(new BenchThread(i));
        }
        for(Future<BigInteger> future : futures){
            results.add(future.get());
        }
        int count = 0;
        for(BigInteger scor : results){
            score = score.add(scor);
            count++;
        }
        score = score.divide(new BigInteger(threads + ""));
        stop.set(false);
        long etime = System.currentTimeMillis() - stime;
        simpMessagingTemplate.convertAndSend("/maint/bench/time", "" + etime);
        System.out.println("CPU bench finished in " + etime + " milliseconds");
        return score.divide(new BigInteger(count + ""));
    }

    private BigInteger CpuBench(int id){
        List<BigInteger> result = new ArrayList<>();
        result.add(primeCount(new BigInteger("2500")));
        result.add(primeTime(20000));
        if(!inttime.get()){
            result.add(intTime(20000));
            inttime.set(true);
        }
        result.add(hashCount(new BigInteger("1000")));
        if(!intcount.get()){
            result.add(intCount(20000));
            intcount.set(true);
        }
        result.add(hashTime(20000));

        result.add(RandomBytes(1024 * 1024, 1000));
        if(intcount.get()){
            result.add(intCount(20000));
            intcount.set(true);
        }
        if(inttime.get()){
            result.add(intTime(20000));
            inttime.set(true);
        }
        result.add(intCount(20000));
        BigInteger score = result.get(0).add(result.get(1));
        score = score.add(result.get(2));
        score = score.add(result.get(3));
        score = score.add(result.get(4));
        score = score.add(result.get(5));
        score = score.add(result.get(6));
        //score = score.divide(new BigInteger("7"));
        System.out.println("Bench id " + id + " score = " + score.toString());
        return score;
    }

    private BigInteger primeCount(BigInteger howMany){
        System.out.println("Starting prime count bench init value = " + howMany.toString() + " primes");
        if(!primecount.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting prime count bench init value = " + howMany.toString() + " primes");
            primecount.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger count = new BigInteger("2");
        BigInteger start = new BigInteger("4");
        while(count.compareTo(howMany) == -1){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            boolean prime = true;
            for(BigInteger i = new BigInteger("2"); i.compareTo(start) == -1; i = i.add(new BigInteger("1"))){
                if(stop.get()){
                    System.out.println("Bench Stopped");
                    return new BigInteger("0");
                }
                if(start.mod(i).equals(new BigInteger("0"))){
                    prime = false;
                }
            }
            if(prime){
                count = count.add(new BigInteger("1"));
                if(count.mod(new BigInteger("50")).equals(new BigInteger("0"))){
                    //System.out.println("Prime #" + count.toString() + " = " + start.toString());
                }
                //
            }
            start = start.add(new BigInteger("1"));
        }
        startTime = System.currentTimeMillis() - startTime;
        System.out.println("Finished prime count bench init value = " + howMany.toString() + " primes in " + startTime + " milliseconds");
        if(!primecounte.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished prime count bench init value = " + howMany.toString() + " primes in " + startTime + " milliseconds");
            primecounte.set(true);
        }
        BigInteger intScore = new BigInteger(startTime + "");
        BigInteger base = new BigInteger("10000000");
        return base.divide(intScore);
    }

    private BigInteger primeTime(long time){
        System.out.println("Starting prime time bench init value = " + time + " milliseconds");
        if(!primetime.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting prime time bench init value = " + time + " milliseconds");
            primetime.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger count = new BigInteger("2");
        BigInteger start = new BigInteger("4");
        while((System.currentTimeMillis() - startTime) < time){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            boolean prime = true;
            for(BigInteger i = new BigInteger("2"); i.compareTo(start) == -1; i = i.add(new BigInteger("1"))){
                if(stop.get()){
                    System.out.println("Bench Stopped");
                    return new BigInteger("0");
                }
                if(start.mod(i).equals(new BigInteger("0"))){
                    prime = false;
                }
            }
            if(prime){
                count = count.add(new BigInteger("1"));
            }
            start = start.add(new BigInteger("1"));
        }
        System.out.println("Finished prime time bench init value = " + time + " milliseconds. Found " + count.toString() + " primes");
        if(!primetimee.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished prime time bench init value = " + time + " milliseconds. Found " + count.toString() + " primes");
            primetimee.set(true);
        }
        return count;
    }

    private BigInteger intTime(long time){
        System.out.println("Starting integer math time bench init value = " + time + " milliseconds");
        if(!inttime.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting integer math time bench init value = " + time + " milliseconds");
            inttime.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger count = new BigInteger("2");
        BigInteger start = new BigInteger("4");
        while((System.currentTimeMillis() - startTime) < time){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            for(BigInteger i = new BigInteger("1"); i.compareTo(new BigInteger("2048")) == -1; i = i.add(new BigInteger("1"))){
                BigInteger temp = new BigInteger(count.toString());
                temp = temp.multiply(i);
                temp = temp.divide(new BigInteger("2"));
            }
            count = count.add(new BigInteger("1"));
        }
        System.out.println("Finished prime time bench init value = " + time + " milliseconds. Found " + count.toString() + " primes");
        if(!inttimee.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished integer math time bench init value = " + time + " milliseconds. Executed " + count.toString() + " iterations");
            inttimee.set(true);
        }
        return count;
    }

    private BigInteger intCount(int count){
        System.out.println("Starting integer math count bench init value = " + count + " iterations");
        if(!intcount.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting integer math count bench init value = " + count + " iterations");
            inttime.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger counti = new BigInteger("2");
        BigInteger start = new BigInteger("4");
        while(counti.intValue() < count){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            for(BigInteger i = new BigInteger("1"); i.compareTo(new BigInteger("2048")) == -1; i = i.add(new BigInteger("1"))){
                BigInteger temp = new BigInteger(counti.toString());
                temp = temp.multiply(i);
                temp = temp.divide(new BigInteger("2"));
            }
            counti = counti.add(new BigInteger("1"));
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println("Finished integer math count bench init value = " + count + " iterations in " + time + " milliseconds");
        if(!intcounte.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished integer math count bench init value = " + count + " iterations in " + time + " milliseconds");
            intcounte.set(true);
        }
        BigInteger intScore = new BigInteger(time + "");
        BigInteger base = new BigInteger("10000000");
        return base.divide(intScore);
    }

    private BigInteger hashCount(BigInteger howMany){
        System.out.println("Starting SHA-256 count bench init value = " + howMany.toString() + " random 1 MB hashes");
        if(!hashcount.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting SHA-256 count bench init value = " + howMany.toString() + " random 1 MB hashes");
            hashcount.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger count = new BigInteger("0");
        BigInteger hash = new BigInteger("1");
        while(count.compareTo(howMany) == -1){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            hash = generateNewHash(hash);
            count = count.add(new BigInteger("1"));
        }
        long end = System.currentTimeMillis() - startTime;
        System.out.println("Finished SHA-256 count bench init value = " + howMany.toString() + " random 1 MB hashes calculated in " + end + " milliseconds");
        if(!hashcounte.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished SHA-256 count bench init value = " + howMany.toString() + " random 1 MB hashes calculated in " + end + " milliseconds");
            hashcounte.set(true);
        }
        BigInteger intScore = new BigInteger(end + "");
        BigInteger base = new BigInteger("10000000");
        return base.divide(intScore);
    }

    private BigInteger hashTime(long time){
        System.out.println("Starting SHA-256 time bench init value = " + time + " milliseconds");
        if(!hashtime.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Starting SHA-256 time bench init value = " + time + " milliseconds");
            hashtime.set(true);
        }
        long startTime = System.currentTimeMillis();
        BigInteger count = new BigInteger("0");
        BigInteger hash = new BigInteger("1");
        long ctime = (long)0;
        while(ctime < time){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            hash = generateNewHash(hash);
            count = count.add(new BigInteger("1"));
            ctime = System.currentTimeMillis() - startTime;
        }
        System.out.println("Finished SHA-256 time bench init value = " + time + " milliseconds " + count.toString() + " random 1 MB hashes calculated in " + ctime + " milliseconds");
        if(!hashtimee.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished SHA-256 time bench init value = " + time + " milliseconds " + count.toString() + " random 1 MB hashes calculated in " + ctime + " milliseconds");
            hashtimee.set(true);
        }
        return count;
    }

    private BigInteger RandomBytes(int size, int howMany){
        long stime = System.currentTimeMillis();
        System.out.println("Started random bytes generation bench init value: size = " + size + " : target count =  " + howMany);
        if(!randombytes.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Started random bytes generation bench init value: size = " + size + " : target count =  " + howMany);
            randombytes.set(true);
        }
        int count = 0;
        while(count < howMany){
            if(stop.get()){
                System.out.println("Bench Stopped");
                return new BigInteger("0");
            }
            byte[] bytes = generateRandomBytes(size);
            count++;
            bytes = null;
        }
        long t = System.currentTimeMillis() - stime;
        System.out.println("Finished random bytes generation bench init value: size = " + size + " : target count =  " + howMany + " in " + t + " milliseconds");
        if(!randombytese.get()){
            simpMessagingTemplate.convertAndSend("/maint/bench/stat", "Finished random bytes generation bench init value: size = " + size + " : target count =  " + howMany + " in " + t + " milliseconds");
            randombytese.set(true);
        }
        BigInteger intScore = new BigInteger(t + "");
        BigInteger base = new BigInteger("10000000");
        return base.divide(intScore);
    }

    private static BigInteger generateNewHash(BigInteger prevHash){
        byte[] bytes = generateRandomBytes(1024 * 1024);
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            bytes = null;
            byte[] byteData = md.digest();
            md.update(byteData);
            md.update(prevHash.toByteArray());
            byteData = md.digest();
            return new BigInteger(byteData);
        }catch (Exception e){
            return new BigInteger("-1");
        }
    }

    private static byte[] generateRandomBytes(int size){
        byte[] bytes = new byte[size];
        Random random = new Random();
        for(int i = 0; i < size; i++){
            bytes[i] = (byte)random.nextInt(125);
        }
        return bytes;
    }

    private class BenchThread implements Callable<BigInteger>{

        private int id = -1;

        public BenchThread(int id){
            this.id = id;
        }

        public BigInteger call(){
            Thread.currentThread().setName("CPU Bench Thread " + id);
            return CpuBench(id);
        }

    }
}
