package com.atguigu.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    /**
     * 正常访问
     * @param id
     * @return
     */
    public String paymentInfo_ok(Integer id){
        return "线程池:"+Thread.currentThread().getName()+" paymentInfo_ok, id :"+id+"\t"+"成功";
    }


    @HystrixCommand(fallbackMethod = "paymentInfo_TimeOutHandler",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public String paymentInfo_timeout(Integer id){

//        int a=10/0;

        int time=5;
        try {
            TimeUnit.SECONDS.sleep(time);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "线程池:"+Thread.currentThread().getName()+" paymentInfo_timeout, id :"+id+"\t异常";

    }
    public String paymentInfo_TimeOutHandler(Integer id){
        return "调用支付接口超时或异常：\t"+"\t当前线程池名字" +Thread.currentThread().getName();

    }

    //==============================服务熔断============================


    @HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback",commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled",value = "true"),//是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"),//请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "10000"),//时间窗口期
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "60"),//失败后达到多少后跳闸
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id){
        if(id < 0){
            throw new RuntimeException("*****id 不能为负数");
        }
        String serialNumber= IdUtil.simpleUUID();
        return  Thread.currentThread().getName()+"\t"+"调用成功，流水号 ："+serialNumber;
    }
    public String paymentCircuitBreaker_fallback(@PathVariable("id") Integer id){
        return "id 不能为负数，请稍后再试，id:"+id;
    }

}
