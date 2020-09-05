package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categories = categoryService.getLevel1Categories();

        model.addAttribute("categories", categories);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        RLock rLock = redissonClient.getLock("my-lock");
        rLock.lock();
        try {
            System.out.println("加锁成功 " + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("释放锁 " + Thread.currentThread().getId());
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            rLock.lock();
            System.out.println("写锁加锁成功 " + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放 " + Thread.currentThread().getId());
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功 " + Thread.currentThread().getId());
            s = stringRedisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放 " + Thread.currentThread().getId());
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("park");
        // semaphore.acquire();
        boolean b = semaphore.tryAcquire();
        if (b) {

        } else {
            return "error";
        }
        return "ok";
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() {
        RSemaphore semaphore = redissonClient.getSemaphore("park");
        semaphore.release();
        return "ok";
    }

    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();

        return "放假了...";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();

        return id + "班的人都走了...";
    }

}
