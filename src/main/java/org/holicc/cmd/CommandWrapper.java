package org.holicc.cmd;

import org.holicc.cmd.annotation.Command;
import org.holicc.parser.RedisValue;
import org.holicc.server.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record CommandWrapper(JedisCommand instance,
                             Method method) {

    public Response execute(List<RedisValue> params) {
        try {
            if (method == null) return Response.Error("command not found");
            Command annotation = method.getAnnotation(Command.class);
            if (annotation.minimumArgs() > params.size()) {
                return Response.Error("wrong number args of [" + annotation.name() + "], see " + annotation.description());
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            List<Object> param = new ArrayList<>();
            for (Class<?> parameterType : parameterTypes) {
                if (parameterType.equals(String.class)) {
                    param.add(params.isEmpty() ? "" : params.remove(0).getValueAsString());
                } else if (parameterType.equals(String[].class)) {
                    param.add(params.isEmpty() ? null : params.stream().map(RedisValue::getValueAsString).toArray(String[]::new));
                } else {
                    param.add(params.isEmpty() ? null : params.remove(0).getValueAsString());
                }
            }
            Object invoke = method.invoke(instance, param.toArray());
            if (invoke instanceof String) {
                return Response.BulkStringReply((String) invoke);
            } else if (invoke instanceof Collection) {
                return Response.ArrayReply((Collection<?>) invoke);
            } else if (invoke instanceof Long || invoke instanceof Integer) {
                return Response.IntReply((int) invoke);
            } else {
                return Response.NullBulkResponse();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Response.Error(e.getMessage());
        }
    }
}
