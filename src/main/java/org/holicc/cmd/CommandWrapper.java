package org.holicc.cmd;

import org.holicc.cmd.annotation.*;
import org.holicc.server.Arguments;
import org.holicc.server.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public record CommandWrapper(FunDBCommand instance,
                             boolean persistence,
                             Method method) {

    public Response execute(LinkedList<Object> redisValue, Arguments pool) {
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
                Inject inject = parameter.getAnnotation(Inject.class);
                // more dynamic args, eg: SocketChannel
                if (Objects.nonNull(inject)) {
                    Object o = pool.get(parameterType);
                    if (inject.required() && Objects.isNull(o))
                        throw new NullPointerException("value is required");
                    args.add(o);
                } else {
                    args.add(redisValue.isEmpty() ? null : caseToArg(redisValue, parameterType));
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
        } catch (
                InvocationTargetException e) {
            return Response.Error(e.getTargetException().getMessage());
        } catch (
                IllegalAccessException e) {
            return Response.Error(e.getMessage());
        }
    }

    private Object caseToArg(LinkedList<Object> redisValue, Class<?> type) {
        if (type.equals(String.class)) {
            return redisValue.pop().toString();
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(redisValue.pop().toString());
        }
        return null;
    }
}
