package org.holicc.cmd;

import org.holicc.cmd.annotation.*;
import org.holicc.datastruct.SortNode;
import org.holicc.server.Arguments;
import org.holicc.server.Response;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

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
                    args.add(caseToArg(redisValue, parameter));
                }
            }
            Class<?> returnType = method.getReturnType();
            Object invoke = method.invoke(instance, args.toArray());
            // no reply eg:SUBSCRIBE
            if (returnType.equals(Void.TYPE)) {
                return null;
            } else if (returnType.isAssignableFrom(String.class)) {
                return Response.BulkStringReply((String) invoke);
            } else if (Collection.class.isAssignableFrom(returnType)) {
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

    private Object caseToArg(LinkedList<Object> redisValue, Parameter type) {
        return switch (type.getType()) {
            case Class<?> x && x.equals(String.class) -> getOrDefault(redisValue, Object::toString, "");
            case Class<?> x && x.equals(String[].class) -> redisValue.isEmpty() ? null : redisValue.stream().map(Object::toString).toArray(String[]::new);
            case Class<?> x && (x.equals(int.class)) -> getOrDefault(redisValue, (o) -> Integer.parseInt(o.toString()), 0);
            case Class<?> x && (x.equals(SortNode[].class)) -> caseToSortNode(redisValue);
            default -> redisValue.isEmpty() ? null : redisValue.pop();
        };
    }

    private <T> T getOrDefault(LinkedList<Object> list, Function<Object, T> apply, T t) {
        return list.isEmpty() ? t : apply.apply(list.pop());
    }

    private SortNode[] caseToSortNode(LinkedList<Object> link) {
        if (link.size() % 2 == 0) {
            int len = link.size() / 2;
            SortNode[] sortNodes = new SortNode[len];
            for (int i = 0; i < sortNodes.length; i++) {
                float score = Float.parseFloat(link.pop().toString());
                String value = link.pop().toString();
                sortNodes[i] = new SortNode(value, score);
            }
            return sortNodes;
        }
        return null;
    }
}
