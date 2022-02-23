package okhttp3.spring.boot;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;

public enum RequestMethod {

    /**
     * get request.
     */
    GET("GET", (builder, bodyStr)->{
        return builder.get();
    }),
    /**
     * head request.
     */
    HEAD("HEAD", (builder, bodyStr)->{
        return builder.head();
    }),
    /**
     * post request.
     */
    POST("POST", (builder, bodyStr)->{
        return builder.post(RequestBody.create(OkHttp3Template.APPLICATION_JSON_UTF8, bodyStr));
    }),
    /**
     * put request.
     */
    PUT("PUT", (builder, bodyStr)->{
        return builder.put(RequestBody.create(OkHttp3Template.APPLICATION_JSON_UTF8, bodyStr));
    }),
    /**
     * patch request.
     */
    PATCH("PATCH", (builder, bodyStr)->{
        return builder.patch(RequestBody.create(OkHttp3Template.APPLICATION_JSON_UTF8, bodyStr));
    }),
    /**
     * delete request.
     */
    DELETE("DELETE", (builder, bodyStr)->{
        return StringUtils.hasText(bodyStr) ? builder.delete(RequestBody.create(OkHttp3Template.APPLICATION_JSON_UTF8, bodyStr)) : builder.delete();
    }),
    /**
     * options request.
     */
    OPTIONS("OPTIONS", (builder, bodyStr)->{
        return builder;
    }),
    /**
     * trace request.
     */
    TRACE("TRACE", (builder, bodyStr)->{
        return builder;
    });

    private String name;
    private BiFunction<Request.Builder, String, Request.Builder> function;
    RequestMethod(String name, BiFunction<Request.Builder, String, Request.Builder> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Request.Builder apply(Request.Builder builder, String bodyStr){
        return function.apply(builder, bodyStr);
    }

    public Request.Builder apply(Request.Builder builder){
        return function.apply(builder, null);
    }

    public static RequestMethod getByName(int name) {
        for (RequestMethod type : RequestMethod.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
