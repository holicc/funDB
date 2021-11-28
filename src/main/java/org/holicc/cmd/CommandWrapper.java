package org.holicc.cmd;

import org.holicc.cmd.annotation.Command;
import org.holicc.cmd.annotation.Inject;
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

    public Response execute(RedisValue redisValue, Arguments pool) {
        try {
            if (method == null) return Response.Error("command not found");
            Command annotation = method.getAnnotation(Command.class);
            if (annotation.minimumArgs() > redisValue.size()) {
                return Response.Error("wrong number args of [" + annotation.name() + "], see " + annotation.description());
            }
            Parameter[] parameters = method.getParameters();
            List<Object> param = new ArrayList<>();
            for (Parameter parameter : parameters) {
                Class<?> parameterType = parameter.getType();
                Inject inject = parameter.getAnnotation(Inject.class);
                // more dynamic args, eg: SocketChannel
                if (Objects.nonNull(inject)) {
                    Object o = pool.get(parameterType);
                    if (inject.required() && Objects.isNull(o))
                        throw new NullPointerException("value is required");
                    param.add(o);
                }
                param.add(redisValue.into(parameterType));
            }
            Class<?> returnType = method.getReturnType();
            Object invoke = method.invoke(instance, param.toArray());
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
