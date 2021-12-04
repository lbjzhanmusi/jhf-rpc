package jhf.protocol.serializer;

public interface Serializer {
    <T> byte[] serialize(T object);
    <T> T deserialize(Class<T> tClass,byte[] bytes);
}
