package org.holicc.cmd;

import org.holicc.cmd.annotation.*;
import org.holicc.protocol.RedisValue;
import org.holicc.server.Arguments;
import org.holicc.server.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record CommandWrapper(FunDBCommand instance,
                             boolean persistence,
                             Method method) {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    public static final String OPTIONS = "options";

    public Response execute(RedisValue redisValue, Arguments pool) {
        try {
            if (method == null) return Response.Error("command not found");
            Command annotation = method.getAnnotation(Command.class);
            if (annotation.minimumArgs() > redisValue.size()) {
                return Response.Error("wrong number args of [" + annotation.name() + "], see " + annotation.description());
            }
            Parameter[] parameters = method.getParameters();
            List<Object> args = new ArrayList<>();
            for (Parameter parameter : parameters) {
                Class<?> parameterType = parameter.getType();
                String parameterName = parameter.getName();
                if (parameterName.equalsIgnoreCase(KEY)) {
                    args.add(redisValue.key());
                } else if (parameterName.equalsIgnoreCase(VALUE)) {
                    args.add(redisValue.value());
                } else if (parameterName.equalsIgnoreCase(OPTIONS)) {
                    args.add(redisValue.options());
                } else {
                    Inject inject = parameter.getAnnotation(Inject.class);
                    // more dynamic args, eg: SocketChannel
                    if (Objects.nonNull(inject)) {
                        Object o = pool.get(parameterType);
                        if (inject.required() && Objects.isNull(o))
                            throw new NullPointerException("value is required");
                        args.add(o);
                    }
                }
            }
            Class<?> returnType = method.getReturnType();
            Object invoke = method.invoke(instance, args.toArray());
            // no reply eg:SUBSCRIBE
            if (returnType.equals(Void.TYPE)) {
                return null;
            } else if (returnType.isAssignableFrom(String.class)) {
                return Response.BulkStringReply((String) invoke);
            } else if (returnType.isAssignableFrom(Collection.class)) {
                return Response.ArrayReply((Collection<?>) invoke);
            } else if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                return Response.IntReply((int) invoke);
            } else if (returnType.equals(long.class) || returnType.equals(Long.class)) {
                return Response.IntReply((long) invoke);
            } else {
                return Response.NullBulkResponse();
            }
        } catch (InvocationTargetException e) {
            return Response.Error(e.getTargetException().getMessage());
        } catch (IllegalAccessException e) {
            return Response.Error(e.getMessage());
        }
    }
}
