package jhf.protocol.serializer;

import com.google.gson.*;
import lombok.SneakyThrows;
import jhf.message.Message;
import jhf.message.RpcRequestMessage;
import jhf.utils.ServicesFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public enum SerializerAlgortithm implements Serializer{
    Java{
        @Override
        public <T> byte[] serialize(T object) {
            try {
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                ObjectOutputStream oos=new ObjectOutputStream(bos);
                oos.writeObject(object);
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("JDK序列化失败");
            }
        }
        @Override
        public <T> T deserialize(Class<T> tClass, byte[] bytes) {
            try {
                ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(bytes));
                return (T)ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("JDK反序列失败");
            }
        }
    },
    Json{
        @Override
        public <T> byte[] serialize(T object) {
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Class.class, new ClassCodec())
                    .create();
            String json=gson.toJson(object);
            return json.getBytes(StandardCharsets.UTF_8);
        }
        @Override
        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
            String s=new String(bytes, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Class.class, new ClassCodec())
                    .create();
            return gson.fromJson(s,clazz);
        }
        //gson没有办法序列化class对象，需要额外处理
        class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
            // 反序列化
            @SneakyThrows
            @Override
            public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                String clazz = jsonElement.getAsString();
                return Class.forName(clazz);
            }
            // 序列化
            @Override
            public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
                // 将 Class 变为 json
                return new JsonPrimitive(aClass.getName());
            }
        }
    };
    //对序列化方法的测试，要加序列化方法的时候先用这个测试一下
    public static void main(String[] args) {
        RpcRequestMessage msg = new RpcRequestMessage(1,
                "jhf.HelloService",
                "saySomething",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );
        byte[] serialize = Json.serialize(msg);
        RpcRequestMessage deserialize = (RpcRequestMessage)Json.deserialize(Message.getMessageClass(101), serialize);
        System.out.println(deserialize);
    }
}
