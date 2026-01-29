package com.xuenai.aiagent.config.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;

public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    /**
     * 保证 Kryo 实例在多线程下安全
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
       Kryo kryo = new Kryo();
       kryo.setReferences(true);
       kryo.setRegistrationRequired(false);
       kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
       return kryo;
    });


    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kryo.writeClassAndObject(output, value);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Kryo serialization failed",e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            Input input = new Input(bytes);
            @SuppressWarnings("unchecked")
            T t = (T) kryo.readClassAndObject(input);
            return t;
        } catch (Exception e) {
            throw new SerializationException("Kryo deserialization failed",e);
        }
    }
}
