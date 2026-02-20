package study.blog.global.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {
    private static final String LOCK_KEY = "lock";

    private final RedissonClient redisson;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(study.blog.global.lock.DistributedLock)")
    public Object around(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock annotation = method.getAnnotation(DistributedLock.class);

        String key = LOCK_KEY + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), annotation.key());
        RLock rLock = redisson.getLock(key);

        try{
            boolean available = rLock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());
            if(!available){
                return false;
            }

            return aopForTransaction.proceed(joinPoint);
        }catch (InterruptedException e){
            throw new InterruptedException();
        }finally {
            if(rLock.isHeldByCurrentThread()){
                rLock.unlock();
            }
        }
    }
}
